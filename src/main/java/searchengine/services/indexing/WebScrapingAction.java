package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;
import searchengine.util.HtmlParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class WebScrapingAction extends RecursiveAction {

    private static final Logger log = LoggerFactory.getLogger(WebScrapingAction.class);
    private final String url;
    private final Integer siteId;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;

    @Override
    protected void compute() {

        if (pageRepository.existsByPath(url)) {
            log.info("already exists {}", url);
            return;
        }

        HtmlParser parser = new HtmlParser(url);

        Page page = new Page();
        page.setContent(parser.getContent());
        page.setPath(parser.getUrl());
        page.setCode(parser.getCode());
        Optional<Site> siteOptional = siteRepository.findById(siteId);
        Site site = siteOptional.orElseThrow();
        page.setSite(site);
        pageRepository.save(page);

        Set<String> links = parser.getLinks();
        if (links == null) return;
        List<WebScrapingAction> subActions = new ArrayList<>();
        for (String link : links) {
            if (pageRepository.existsByPath(link)) {
                log.info("already exists {}", link);
                continue;
            }
            WebScrapingAction subAction = new WebScrapingAction(link, siteId, pageRepository, siteRepository);
            subAction.fork();
            subActions.add(subAction);
        }

        subActions.forEach(ForkJoinTask::join);
    }
}
