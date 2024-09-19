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
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {

    private static final Logger log = LoggerFactory.getLogger(IndexingServiceImpl.class);
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;
    private ForkJoinPool forkJoinPool;
    private boolean stopRequested;

    @Override
    public void startIndexing() {
        forkJoinPool = new ForkJoinPool();
        for (searchengine.config.Site siteConfig : sitesList.getSites()) {
            String url = siteConfig.getUrl();
            String name = siteConfig.getName();
            deleteSite(url);
            createAndSaveSite(url, name);
        }
        webScrape();
        if (stopRequested) {
            siteRepository.findAll().forEach(site -> {
                site.setLastError("Индексация остановлена пользователем");
                site.setStatus(Status.FAILED);
                siteRepository.save(site);
        });
            return;
        }
        log.info("INDEXING FINISHED");
    }

    @Override
    public void stopIndexing() {
        stopRequested = true;
        forkJoinPool.shutdownNow();
        log.info("INDEXING STOPPED");
    }

    public boolean indexingInProgress() {
        return forkJoinPool != null && (forkJoinPool.getRunningThreadCount() > 0 || forkJoinPool.hasQueuedSubmissions());
    }

    private void webScrape() {
        List<ForkJoinTask<Void>> tasks = siteRepository.findAll().stream()
                .map(site -> new WebScrapingAction(site.getUrl(), site.getId(), pageRepository, siteRepository, true))
                .map(forkJoinPool::submit)
                .toList();
        tasks.forEach(ForkJoinTask::join);
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
