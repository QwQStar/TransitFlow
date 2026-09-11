package com.transitflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "vehicle_location",
        uniqueConstraints = @UniqueConstraint(name = "uk_source_event", columnNames = {"source", "event_id"})
)
public class VehicleLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EventSource source;

    @Column(name = "device_id", nullable = false, length = 64)
    private String deviceId;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "event_time", nullable = false)
    private Instant eventTime;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lon;

    @Column(length = 512)
    private String payload;

    @Column(name = "ingested_at", nullable = false)
    private Instant ingestedAt;

    public static VehicleLocation from(MobilityEvent event) {
        VehicleLocation row = new VehicleLocation();
        row.source = event.source();
        row.deviceId = event.deviceId();
        row.eventId = event.eventId();
        row.eventTime = event.eventTime();
        row.lat = event.lat();
        row.lon = event.lon();
        row.payload = event.payload();
        row.ingestedAt = Instant.now();
        return row;
    }

    public Long getId() {
        return id;
    }

    public EventSource getSource() {
        return source;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getEventId() {
        return eventId;
    }

    public Instant getEventTime() {
        return eventTime;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String getPayload() {
        return payload;
    }

    public Instant getIngestedAt() {
        return ingestedAt;
    }
}
