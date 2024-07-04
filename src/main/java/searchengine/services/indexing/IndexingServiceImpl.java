package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import searchengine.config.SitesList;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;

    @Override
    public void startIndexing() {

        for (searchengine.config.Site siteConfig : sitesList.getSites()) {
            String url = siteConfig.getUrl();
            String name = siteConfig.getName();
            deleteSite(url);
            createAndSaveSite(url, name);
        }

        WebScrapingAction action = new WebScrapingAction("https://www.afisha.uz/ru", 3, pageRepository, siteRepository);
        action.invoke();
        log.info("indexing finished");
    }

    private void deleteSite(String url) {
        siteRepository.deleteByUrl(url);
    }

    private void createAndSaveSite(String url, String name) {
        Site site = new Site();
        site.setStatus(Status.INDEXING);
        site.setName(name);
        site.setUrl(url);
        site.setStatusTime(LocalDateTime.now());
        siteRepository.save(site);
    }
}
