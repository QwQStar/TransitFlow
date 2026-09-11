package com.transitflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MobilityEvent(
        @NotNull EventSource source,
        @NotBlank String deviceId,
        @NotBlank String eventId,
        @NotNull Instant eventTime,
        double lat,
        double lon,
        String payload
) {
    public String uniqueKey() {
        return source.name() + ":" + eventId;
    }

    public String geoMember() {
        return source.name() + ":" + deviceId;
    }

    public void validateForIngest() {
        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            throw new IllegalArgumentException("Coordinates out of range: lat=" + lat + ", lon=" + lon);
        }
        if ("POISON".equalsIgnoreCase(payload)) {
            throw new IllegalArgumentException("Poison payload for DLQ demo, eventId=" + eventId);
        }
    }
}
