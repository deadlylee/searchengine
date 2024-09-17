package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.HtmlParser;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebScrapingAction extends RecursiveAction {

    private static final Logger log = LoggerFactory.getLogger(WebScrapingAction.class);
    private final String urlString;
    private final Integer siteId;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final boolean isRootTask;

    @SneakyThrows(MalformedURLException.class)
    @Override
    protected void compute() {

        URL url = new URL(urlString);
        String path = url.getPath().isEmpty() ? "/" : url.getPath();

        if (pageRepository.existsByPath(path)) {
            log.info("already exists {}", path);
            return;
        }

        HtmlParser parser = new HtmlParser(urlString);

        Page page = new Page();
        page.setContent(parser.getContent());
        page.setPath(path);
        page.setCode(parser.getCode());
        Site site = updateStatusTime();
        page.setSite(site);
        pageRepository.save(page);

        Set<String> links = parser.getLinks();
        if (links == null) return;
        List<WebScrapingAction> subActions = new ArrayList<>();
        for (String link : links) {

            WebScrapingAction subAction = new WebScrapingAction(link, siteId, pageRepository, siteRepository, false);
            subAction.fork();
            subActions.add(subAction);
        }

         try {
             subActions.forEach(ForkJoinTask::join);
         } catch (CancellationException e) {
             log.info("TASK CANCELLED");
         }

        if (isRootTask) {
            updateStatus();
        }
    }

    private void updateStatus() {
        Site site = siteRepository.findById(siteId).orElseThrow();
        site.setStatus(Status.INDEXED);
        siteRepository.save(site);
    }

    private Site updateStatusTime() {
        Site site = siteRepository.findById(siteId).orElseThrow();
        site.setStatusTime(LocalDateTime.now());
        return siteRepository.save(site);
    }
}
