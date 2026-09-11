package com.transitflow.reconcile;

import com.transitflow.store.IngestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReconciliationJob {

    private static final Logger log = LoggerFactory.getLogger(ReconciliationJob.class);

    private final IngestMetrics metrics;

    public ReconciliationJob(IngestMetrics metrics) {
        this.metrics = metrics;
    }

    @Scheduled(fixedDelayString = "${transitflow.reconcile-ms}")
    public void reconcile() {
        IngestMetrics.Snapshot snap = metrics.snapshot();
        long drift = snap.drift();
        if (Math.abs(drift) > 5) {
            log.warn("ingest drift={} consumed={} landed={} duplicate={} dlq={}",
                    drift, snap.consumed(), snap.landed(), snap.duplicate(), snap.dlq());
        } else {
            log.info("ingest in balance drift={} consumed={} landed={} duplicate={} dlq={}",
                    drift, snap.consumed(), snap.landed(), snap.duplicate(), snap.dlq());
        }
    }
}
