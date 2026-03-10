package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.repository.WarehouseRepository;
import ftn.siit.nvt.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import com.influxdb.client.domain.WritePrecision;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseTemperatureService {

    private final WarehouseRepository warehouseRepository;
    private final SectorRepository sectorRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    @Transactional
    public void recordTemperatureReading(Long warehouseId, Long sectorId, Double temperature) {
        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));

        if (!sector.getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Sector doesn't belong to warehouse");
        }

        sector.setLastTemperature(temperature);
        sector.setLastTemperatureReadingAt(LocalDateTime.now());
        sectorRepository.save(sector);

        Warehouse warehouse = sector.getWarehouse();
        LocalDateTime now = LocalDateTime.now();

        Point point = Point
                .measurement("warehouse_temperature")
                .addTag("warehouse_id", warehouseId.toString())
                .addTag("sector_id", sectorId.toString())
                .addTag("city", warehouse.getCity().getName())
                .addTag("country", warehouse.getCountry().getCode())
                .addField("temperature", temperature)
                .time(now.atZone(ZoneId.systemDefault()).toInstant(), WritePrecision.MS);

        // 3. Upiši u InfluxDB
        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
//            log.info("Temperature recorded in InfluxDB: Warehouse {}, Sector {}, Temp: {}°C",
//                    warehouseId, sectorId, temperature);
        } catch (Exception e) {
            log.error("Failed to write temperature metrics to Influx for warehouse {} sector {}",
                    warehouseId, sectorId, e);
        }
    }


    public void recordAvailability(Warehouse warehouse,boolean isOnline, Instant timestamp, String reason) {
        Point point = Point
                .measurement("warehouse_availability")
                .addTag("warehouse_id", warehouse.getId().toString())
                .addField("online", isOnline)
                .addField("status", "heartbeat_received")
                .time(Instant.now(), WritePrecision.MS);

        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
//            log.info("Availability recorded in InfluxDB: Warehouse {}, Online: {}, Reason: {}",
//                    warehouse.getId(), isOnline, reason);
        } catch (Exception e) {
            log.error("Failed to write availability metrics to Influx for warehouse {}",
                    warehouse.getId(), e);
        }
    }
}
