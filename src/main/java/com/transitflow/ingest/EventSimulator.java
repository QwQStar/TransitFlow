package com.transitflow.ingest;

import com.transitflow.config.AppProperties;
import com.transitflow.domain.EventSource;
import com.transitflow.domain.MobilityEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Publishes simulated GPS / weigh-in-motion / flight events around Singapore
 * so the pipeline can be demoed without a live device feed.
 */
@Component
@ConditionalOnProperty(prefix = "transitflow.simulator", name = "enabled", havingValue = "true")
public class EventSimulator {

    private static final Logger log = LoggerFactory.getLogger(EventSimulator.class);

    /** Roughly downtown Singapore — demo coordinates only. */
    private static final double ORIGIN_LAT = 1.3521;
    private static final double ORIGIN_LON = 103.8198;

    private final EventPublisher publisher;
    private final AppProperties properties;

    public EventSimulator(EventPublisher publisher, AppProperties properties) {
        this.publisher = publisher;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${transitflow.simulator.interval-ms}")
    public void publishBatch() {
        for (EventSource source : EventSource.values()) {
            publishOne(source, false);
        }
        if (ThreadLocalRandom.current().nextDouble() < properties.simulator().poisonRate()) {
            publishOne(EventSource.GPS, true);
        }
    }

    private void publishOne(EventSource source, boolean poison) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        int n = Math.max(1, properties.simulator().devicesPerSource());
        String deviceId = source.name().toLowerCase() + "-" + (rnd.nextInt(n) + 1);
        MobilityEvent event = new MobilityEvent(
                source,
                deviceId,
                UUID.randomUUID().toString(),
                Instant.now(),
                ORIGIN_LAT + rnd.nextDouble(-0.08, 0.08),
                ORIGIN_LON + rnd.nextDouble(-0.08, 0.08),
                poison ? "POISON" : source.name() + " sample"
        );
        publisher.publish(event);
        if (poison) {
            log.info("injected poison event device={}", deviceId);
        }
    }
}
