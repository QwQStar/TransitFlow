package com.transitflow.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MobilityEventTest {

    @Test
    void uniqueKeyUsesSourceAndEventId() {
        MobilityEvent event = new MobilityEvent(
                EventSource.GPS, "gps-1", "evt-9", Instant.parse("2026-01-01T00:00:00Z"), 1.35, 103.82, "ok"
        );
        assertEquals("GPS:evt-9", event.uniqueKey());
        assertEquals("GPS:gps-1", event.geoMember());
    }

    @Test
    void poisonPayloadIsRejected() {
        MobilityEvent event = new MobilityEvent(
                EventSource.WIM, "wim-1", "evt-1", Instant.parse("2026-01-01T00:00:00Z"), 1.35, 103.82, "POISON"
        );
        assertThrows(IllegalArgumentException.class, event::validateForIngest);
    }

    @Test
    void outOfRangeCoordinatesAreRejected() {
        MobilityEvent event = new MobilityEvent(
                EventSource.FLIGHT, "flt-1", "evt-1", Instant.parse("2026-01-01T00:00:00Z"), 120, 103.82, "ok"
        );
        assertThrows(IllegalArgumentException.class, event::validateForIngest);
    }
}
