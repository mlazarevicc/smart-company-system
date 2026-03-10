package ftn.siit.nvt.service;

import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseHeartbeatService {

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final WarehouseRepository warehouseRepository;
    private final WarehouseTemperatureService temperatureService;

    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    @Transactional
    public void checkWarehouseAvailability() {
        log.debug("Checking warehouse heartbeat availability");

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);

        List<Warehouse> offlineCandidates = warehouseRepository.findByIsOnlineFalse();

        // Find warehouses that should be online but didn't send heartbeat
        offlineCandidates.addAll(warehouseRepository.findAll()
                .stream()
                .filter(w -> w.getIsOnline() &&
                        (w.getLastHeartbeat() == null || w.getLastHeartbeat().isBefore(threshold)))
                .toList());

        for (Warehouse warehouse : offlineCandidates) {
            if (warehouse.getIsOnline()) {
                log.debug("Warehouse {} is marked as offline (heartbeat timeout)", warehouse.getId());
                warehouse.setIsOnline(false);
                warehouseRepository.save(warehouse);

                temperatureService.recordAvailability(
                        warehouse,
                        false,
                        Instant.now(),
                        "heartbeat_missed"
                );
            }
        }
    }
}
