package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.indexing.IndexingService;
import searchengine.services.statistics.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

    @GetMapping(value = "/startIndexing", produces = "application/json")
    public ResponseEntity<IndexingResponse> startIndexing() {
        if (indexingService.indexingInProgress()) {
            return ResponseEntity.badRequest().body(new IndexingResponse(false, "Индексация уже запущена"));
        }
        indexingService.startIndexing();
        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        if (!indexingService.indexingInProgress()) {
            return ResponseEntity.badRequest().body(new IndexingResponse(false, "Индексация не запущена"));
        }
        indexingService.stopIndexing();
        return ResponseEntity.ok(new IndexingResponse(true));
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }
}
