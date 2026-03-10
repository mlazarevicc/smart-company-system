package ftn.siit.nvt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.siit.nvt.dto.warehouse.HeartbeatMessage;
import ftn.siit.nvt.dto.warehouse.TemperatureMessage;
import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.SectorRepository;
import ftn.siit.nvt.repository.WarehouseRepository;
import ftn.siit.nvt.service.WarehouseService;
import ftn.siit.nvt.service.WarehouseTemperatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseMessageListener {

    private final WarehouseService warehouseService;
    private final WarehouseTemperatureService temperatureService;
    private final SectorRepository sectorRepository;
    private final WarehouseRepository warehouseRepository;
    private final ObjectMapper objectMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "warehouse-temperature-updates", durable = "true"),
            exchange = @Exchange(value = "smart-manufacturing.warehouses", type = "topic"),
            key = "warehouse-temperature-updates"
    ))    @Transactional
    public void handleTemperatureUpdate(String message) {
        try {
            TemperatureMessage temperatureMessage = objectMapper.readValue(message, TemperatureMessage.class);

            for (TemperatureMessage.SectorTemperature temp : temperatureMessage.getTemperatures()) {
                Sector sector = sectorRepository.findById(temp.getSectorId())
                        .orElseThrow(() -> new RuntimeException("Sector not found: " + temp.getSectorId()));

                sector.setLastTemperature(temp.getTemperature());
                sector.setLastTemperatureReadingAt(temperatureMessage.getTimestamp());
                sectorRepository.save(sector);

                // Record to InfluxDB
                temperatureService.recordTemperatureReading(
                        temperatureMessage.getWarehouseId(),
                        temp.getSectorId(),
                        temp.getTemperature()
                );
            }
        } catch (Exception e) {
            log.error("Error processing temperature update: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "warehouse-heartbeat", durable = "true"),
            exchange = @Exchange(value = "smart-manufacturing.warehouses", type = "topic"),
            key = "warehouse-heartbeat"
    ))    public void handleHeartbeat(String message) {
        try {
            HeartbeatMessage heartbeat = objectMapper.readValue(message, HeartbeatMessage.class);

            warehouseService.recordHeartbeat(heartbeat.getWarehouseId());

            Warehouse warehouse = warehouseRepository.findById(heartbeat.getWarehouseId())
                    .orElseThrow(() -> new RuntimeException("Warehouse not found for heartbeat: " + heartbeat.getWarehouseId()));

            java.time.Instant timestampInstant = heartbeat.getTimestamp()
                    .atZone(ZoneOffset.UTC)
                    .toInstant();

            temperatureService.recordAvailability(
                    warehouse,
                    true,
                    timestampInstant,
                    "heartbeat_received"
            );

        } catch (Exception e) {
            log.error("Error processing heartbeat: {}", e.getMessage(), e);
        }
    }
}
