package com.transitflow.store;

import com.transitflow.domain.EventSource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IngestMetrics {

    public static final String CONSUMED = "transitflow:metrics:consumed";
    public static final String LANDED = "transitflow:metrics:landed";
    public static final String DUPLICATE = "transitflow:metrics:duplicate";
    public static final String DLQ = "transitflow:metrics:dlq";

    private final StringRedisTemplate redis;

    public IngestMetrics(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void markConsumed(EventSource source) {
        incr(CONSUMED, source);
    }

    public void markLanded(EventSource source) {
        incr(LANDED, source);
    }

    public void markDuplicate(EventSource source) {
        incr(DUPLICATE, source);
    }

    public void markDlq() {
        redis.opsForValue().increment(DLQ);
    }

    public Snapshot snapshot() {
        return new Snapshot(readHash(CONSUMED), readHash(LANDED), readHash(DUPLICATE), readLong(DLQ));
    }

    private void incr(String key, EventSource source) {
        redis.opsForHash().increment(key, source.name(), 1);
    }

    private Map<String, Long> readHash(String key) {
        Map<String, Long> out = new LinkedHashMap<>();
        for (EventSource source : EventSource.values()) {
            out.put(source.name(), 0L);
        }
        Map<Object, Object> raw = redis.opsForHash().entries(key);
        raw.forEach((k, v) -> out.put(String.valueOf(k), Long.parseLong(String.valueOf(v))));
        return out;
    }

    private long readLong(String key) {
        String value = redis.opsForValue().get(key);
        return value == null ? 0L : Long.parseLong(value);
    }

    public record Snapshot(
            Map<String, Long> consumed,
            Map<String, Long> landed,
            Map<String, Long> duplicate,
            long dlq
    ) {
        public long drift() {
            long c = consumed.values().stream().mapToLong(Long::longValue).sum();
            long l = landed.values().stream().mapToLong(Long::longValue).sum();
            long d = duplicate.values().stream().mapToLong(Long::longValue).sum();
            return c - l - d - dlq;
        }
    }
}
