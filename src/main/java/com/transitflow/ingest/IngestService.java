package com.transitflow.ingest;

import com.transitflow.domain.MobilityEvent;
import com.transitflow.domain.VehicleLocation;
import com.transitflow.store.DedupService;
import com.transitflow.store.GeoLocationService;
import com.transitflow.store.IngestMetrics;
import com.transitflow.store.VehicleLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestService {

    private static final Logger log = LoggerFactory.getLogger(IngestService.class);

    private final DedupService dedupService;
    private final VehicleLocationRepository repository;
    private final GeoLocationService geoLocationService;
    private final IngestMetrics metrics;

    public IngestService(
            DedupService dedupService,
            VehicleLocationRepository repository,
            GeoLocationService geoLocationService,
            IngestMetrics metrics
    ) {
        this.dedupService = dedupService;
        this.repository = repository;
        this.geoLocationService = geoLocationService;
        this.metrics = metrics;
    }

    @Transactional
    public void ingest(MobilityEvent event) {
        metrics.markConsumed(event.source());
        event.validateForIngest();

        if (!dedupService.firstSeen(event.uniqueKey())) {
            metrics.markDuplicate(event.source());
            log.debug("Redis short-window duplicate {}", event.uniqueKey());
            return;
        }

        try {
            repository.save(VehicleLocation.from(event));
        } catch (DataIntegrityViolationException ex) {
            metrics.markDuplicate(event.source());
            log.debug("MySQL unique-key duplicate {}", event.uniqueKey());
            return;
        }

        geoLocationService.update(event);
        metrics.markLanded(event.source());
    }
}
