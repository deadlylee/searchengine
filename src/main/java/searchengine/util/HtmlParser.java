package searchengine.util;

import lombok.Getter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class HtmlParser {

    private static final Logger log = LoggerFactory.getLogger(HtmlParser.class);
    private final @Getter String url;
    private @Getter int code;
    private Document document;
    private static final String fileExtensionRegex = "\\.(pdf|jpg|jpeg|png|gif|doc|docx|xls|xlsx|ppt|pptx)$";
    private static final Pattern pattern = Pattern.compile(fileExtensionRegex, Pattern.CASE_INSENSITIVE);

    public HtmlParser(String url) {
        this.url = url;
        fetchHtml();
    }

    private void fetchHtml() {

        try {
            log.info("processing now: {}", url);
            Connection connection = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("http://www.google.com")
                    .ignoreHttpErrors(true)
                    .timeout(10 * 1000);

            Connection.Response response = connection.execute();
            code = response.statusCode();
            document = connection.get();
        } catch (UnsupportedMimeTypeException e) {
            log.warn("Unsupported Mime Type: {}", e.getMessage());
        } catch (SocketTimeoutException e) {
            log.warn("{}: {}", e.getMessage(), url);
        } catch (IOException e) {
            log.error("Error occurred: {}", e.getMessage());
        }
    }

    public String getContent() {
        return document != null ? document.html() : "No content";
    }

    public Set<String> getLinks() {

        if (document == null) {
            return null;
        }

        Set<String> links = new HashSet<>();

        Elements elements = document.select("a[href]");
        for (Element element : elements) {
            String link = element.attr("abs:href");
            if (link.startsWith(url) && !pattern.matcher(link).find() && !link.contains("#")) {
                links.add(link);
            }
        }

        return links;
    }
}
