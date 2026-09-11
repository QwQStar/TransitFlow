package com.transitflow.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "transitflow")
public record AppProperties(
        Topics topics,
        Simulator simulator,
        long dedupTtlSeconds,
        long reconcileMs
) {
    public record Topics(String ingest, String dlq) {
    }

    public record Simulator(
            boolean enabled,
            int intervalMs,
            int devicesPerSource,
            double poisonRate
    ) {
    }
}
