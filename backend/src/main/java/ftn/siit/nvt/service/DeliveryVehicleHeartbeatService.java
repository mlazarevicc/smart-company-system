package ftn.siit.nvt.service;

import ftn.siit.nvt.model.DeliveryVehicle;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.repository.DeliveryVehicleRepository;
import ftn.siit.nvt.repository.FactoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryVehicleHeartbeatService {

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final DeliveryVehicleRepository vehicleRepository;
    private final DeliveryVehicleDistanceService distanceService;

    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    @Transactional
    public void checkVehicleAvailability() {
        log.debug("Checking vehicle heartbeat availability");

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);

        List<DeliveryVehicle> offlineCandidates = vehicleRepository.findByIsOnlineFalse();

        // Find factories that should have been online but didn't send a heartbeat
        offlineCandidates.addAll(vehicleRepository.findAll()
                .stream()
                .filter(f -> f.getIsOnline() &&
                        (f.getLastHeartbeat() == null || f.getLastHeartbeat().isBefore(threshold)))
                .toList());

        for (DeliveryVehicle vehicle : offlineCandidates) {
            if (vehicle.getIsOnline()) {
                log.warn("Vehicle {} is marked as offline (heartbeat timeout)", vehicle.getId());
                vehicle.setIsOnline(false);
                vehicleRepository.save(vehicle);

                distanceService.recordAvailability(
                        vehicle.getId(),
                        false,
                        OffsetDateTime.now(),
                        "heartbeat_missed"
                );

                evictVehicleCache(vehicle.getId());
            } else {
                distanceService.recordAvailability(
                        vehicle.getId(),
                        false,
                        OffsetDateTime.now(),
                        "vehicle_shut_down"
                );
            }
        }
    }

    @CacheEvict(value = "vehicle", key = "#vehicleId")
    public void evictVehicleCache(Long vehicleId) {
        // This method will evict the cache for a specific vehicle based on the vehicle ID
        log.debug("Cache evicted for vehicle with ID: {}", vehicleId);
    }
}
