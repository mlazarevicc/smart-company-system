package ftn.siit.nvt.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import com.influxdb.client.domain.WritePrecision;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.WarehouseRepository;
import ftn.siit.nvt.service.WarehouseTemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Slf4j
public class WarehouseAvailabilityChecker {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseTemperatureService temperatureService;

    private static final int OFFLINE_THRESHOLD_SECONDS = 60;

    public WarehouseAvailabilityChecker(WarehouseRepository warehouseRepository, WarehouseTemperatureService temperatureService) {
        this.warehouseRepository = warehouseRepository;
        this.temperatureService = temperatureService;
    }

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void checkAllWarehousesForTimeout() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(OFFLINE_THRESHOLD_SECONDS);

        List<Warehouse> timedOutWarehouses = warehouseRepository.findTimedOutWarehouses(cutoffTime);


        if (timedOutWarehouses.isEmpty()) {
            return;
        }

        for (Warehouse warehouse : timedOutWarehouses) {
            warehouse.setIsOnline(false);

            temperatureService.recordAvailability(
                    warehouse,
                    false,
                    Instant.now(),
                    "heartbeat_timeout"
            );

            log.info("Warehouse {} timed out! Status changed to OFFLINE in PostgreSQL.", warehouse.getId());
        }

        warehouseRepository.saveAll(timedOutWarehouses);
    }
}