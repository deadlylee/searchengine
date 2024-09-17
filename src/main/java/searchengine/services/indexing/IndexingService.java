package searchengine.services.indexing;

public interface IndexingService {

    void startIndexing();

    void stopIndexing();

    boolean indexingInProgress();
}
