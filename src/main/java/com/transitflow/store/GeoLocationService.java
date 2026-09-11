package com.transitflow.store;

import com.transitflow.domain.MobilityEvent;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GeoLocationService {

    public static final String GEO_KEY = "transitflow:geo";

    private final StringRedisTemplate redis;

    public GeoLocationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void update(MobilityEvent event) {
        redis.opsForGeo().add(GEO_KEY, new Point(event.lon(), event.lat()), event.geoMember());
    }

    public List<NearbyHit> nearby(double lat, double lon, double km) {
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redis.opsForGeo().radius(
                GEO_KEY,
                new Circle(new Point(lon, lat), new Distance(km, Metrics.KILOMETERS))
        );
        if (results == null) {
            return List.of();
        }
        return results.getContent().stream().map(NearbyHit::from).toList();
    }

    public record NearbyHit(String member, Double distanceKm) {
        static NearbyHit from(GeoResult<RedisGeoCommands.GeoLocation<String>> result) {
            Double km = result.getDistance() == null ? null : result.getDistance().getValue();
            return new NearbyHit(result.getContent().getName(), km);
        }
    }
}
