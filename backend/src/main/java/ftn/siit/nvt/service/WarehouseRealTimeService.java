package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ftn.siit.nvt.dto.warehouse.RealTimeTemperatureDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.SectorRepository;
import ftn.siit.nvt.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseRealTimeService {

    private final WarehouseRepository warehouseRepository;
    private final SectorRepository sectorRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    public RealTimeTemperatureDTO getInitialTemperatureData(Long warehouseId) {
        log.info("Fetching initial real-time temperature data for warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        RealTimeTemperatureDTO dto = new RealTimeTemperatureDTO();
        dto.setWarehouseId(warehouseId);
        dto.setWarehouseName(warehouse.getName());
        dto.setTimestamp(LocalDateTime.now());
        dto.setIsOnline(warehouse.getIsOnline());
        dto.setLastHeartbeat(warehouse.getLastHeartbeat());

        List<Sector> sectors = sectorRepository.findByWarehouse(warehouse);

        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: -3h)
              |> filter(fn: (r) => r._measurement == "warehouse_temperature")
              |> filter(fn: (r) => r.warehouse_id == "%d")
              |> filter(fn: (r) => r._field == "temperature")
              |> aggregateWindow(every: 5m, fn: mean, createEmpty: false)
              |> keep(columns: ["_time", "_value", "sector_id"])
              |> sort(columns: ["_time"], desc: false)
            """, bucket, warehouseId);

        Map<Long, List<RealTimeTemperatureDTO.TemperatureDataPoint>> sectorDataMap =
                sectors.stream().collect(Collectors.toMap(
                        Sector::getId,
                        s -> new ArrayList<>()
                ));

        List<Double> allTemps = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Long sectorId = Long.parseLong(record.getValueByKey("sector_id").toString());

                    if (sectorDataMap.containsKey(sectorId)) {
                        RealTimeTemperatureDTO.TemperatureDataPoint point =
                                new RealTimeTemperatureDTO.TemperatureDataPoint();

                        Instant instant = record.getTime();
                        if (instant != null) {
                            point.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                        }

                        Object value = record.getValue();
                        if (value instanceof Number) {
                            double temp = ((Number) value).doubleValue();
                            point.setTemperature(Math.round(temp * 100.0) / 100.0);
                            allTemps.add(temp);
                        }

                        sectorDataMap.get(sectorId).add(point);
                    }
                }
            }

            log.info("Retrieved temperature data for {} sectors", sectorDataMap.size());

        } catch (Exception e) {
            log.error("Failed to query real-time temperature data for warehouse {}: {}",
                    warehouseId, e.getMessage(), e);
        }

        List<RealTimeTemperatureDTO.SectorTemperatureData> sectorDataList = new ArrayList<>();

        for (Sector sector : sectors) {
            RealTimeTemperatureDTO.SectorTemperatureData sectorData =
                    new RealTimeTemperatureDTO.SectorTemperatureData();

            sectorData.setSectorId(sector.getId());
            sectorData.setSectorName(sector.getName());
            sectorData.setCurrentTemperature(sector.getLastTemperature());
            sectorData.setLastReading(sector.getLastTemperatureReadingAt());
            sectorData.setDataPoints(sectorDataMap.getOrDefault(sector.getId(), new ArrayList<>()));

            sectorDataList.add(sectorData);
        }

        dto.setSectorData(sectorDataList);

        if (!allTemps.isEmpty()) {
            DoubleSummaryStatistics stats = allTemps.stream()
                    .mapToDouble(Double::doubleValue)
                    .summaryStatistics();

            dto.setAvgTemperature(Math.round(stats.getAverage() * 100.0) / 100.0);
            dto.setMinTemperature(Math.round(stats.getMin() * 100.0) / 100.0);
            dto.setMaxTemperature(Math.round(stats.getMax() * 100.0) / 100.0);
        }

        log.info("Initial temperature data prepared for warehouse {} with {} sectors",
                warehouseId, sectorDataList.size());

        return dto;
    }

    public RealTimeTemperatureDTO getUpdatedTemperatureData(Long warehouseId, Long sectorId) {
        log.info("Getting updated temperature data for warehouse {} sector {}",
                warehouseId, sectorId);

        return getInitialTemperatureData(warehouseId);
    }
}
