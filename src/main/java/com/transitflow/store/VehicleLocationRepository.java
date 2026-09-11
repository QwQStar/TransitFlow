package com.transitflow.store;

import com.transitflow.domain.EventSource;
import com.transitflow.domain.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, Long> {

    Optional<VehicleLocation> findBySourceAndEventId(EventSource source, String eventId);

    long countBySource(EventSource source);
}
