package com.transitflow.api;

import com.transitflow.store.GeoLocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NearbyController {

    private final GeoLocationService geoLocationService;

    public NearbyController(GeoLocationService geoLocationService) {
        this.geoLocationService = geoLocationService;
    }

    @GetMapping("/nearby")
    public List<GeoLocationService.NearbyHit> nearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "5") double km
    ) {
        return geoLocationService.nearby(lat, lon, km);
    }
}
