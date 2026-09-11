package com.transitflow.api;

import com.transitflow.store.IngestMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class StatsController {

    private final IngestMetrics metrics;

    public StatsController(IngestMetrics metrics) {
        this.metrics = metrics;
    }

    @GetMapping("/stats")
    public IngestMetrics.Snapshot stats() {
        return metrics.snapshot();
    }
}
