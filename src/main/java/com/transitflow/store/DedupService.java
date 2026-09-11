package com.transitflow.store;

import com.transitflow.config.AppProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class DedupService {

    static final String KEY_PREFIX = "transitflow:dedup:";

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public DedupService(StringRedisTemplate redis, AppProperties properties) {
        this.redis = redis;
        this.ttl = Duration.ofSeconds(properties.dedupTtlSeconds());
    }

    /**
     * Short-window dedup. {@code true} means this process saw the event first.
     * MySQL unique key is still the source of truth after Redis TTL expires.
     */
    public boolean firstSeen(String uniqueKey) {
        Boolean created = redis.opsForValue().setIfAbsent(KEY_PREFIX + uniqueKey, "1", ttl);
        return Boolean.TRUE.equals(created);
    }
}
