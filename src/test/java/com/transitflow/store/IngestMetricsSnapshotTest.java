package com.transitflow.store;

import com.transitflow.store.IngestMetrics.Snapshot;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngestMetricsSnapshotTest {

    @Test
    void driftIsConsumedMinusLandedMinusDuplicateMinusDlq() {
        Snapshot snap = new Snapshot(
                Map.of("GPS", 10L, "WIM", 5L, "FLIGHT", 5L),
                Map.of("GPS", 8L, "WIM", 5L, "FLIGHT", 5L),
                Map.of("GPS", 1L, "WIM", 0L, "FLIGHT", 0L),
                1L
        );
        assertEquals(0L, snap.drift());
    }
}
