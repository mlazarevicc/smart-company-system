package ftn.siit.nvt.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

import ftn.siit.nvt.dto.warehouse.TemperatureAnalyticsDTO;
import ftn.siit.nvt.dto.warehouse.WarehouseAvailabilityAnalyticsDTO;
import ftn.siit.nvt.dto.warehouse.WarehouseMetricsDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.SectorRepository;
import ftn.siit.nvt.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseAnalyticsService {

    private final WarehouseRepository warehouseRepository;
    private final SectorRepository sectorRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;


    public WarehouseMetricsDTO getCurrentMetrics(Long warehouseId) {
        log.info("Fetching current metrics for warehouse: {}", warehouseId);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        WarehouseMetricsDTO dto = new WarehouseMetricsDTO();
        dto.setWarehouseId(warehouseId);
        dto.setWarehouseName(warehouse.getName());
        dto.setIsOnline(warehouse.getIsOnline());
        dto.setLastHeartbeat(warehouse.getLastHeartbeat());

        List<Sector> sectors = sectorRepository.findByWarehouse(warehouse);
        dto.setTotalSectors(sectors.size());

        // Calculate temperature stats from sectors
        if (!sectors.isEmpty()) {
            List<Double> temps = sectors.stream()
                    .map(Sector::getLastTemperature)
                    .filter(Objects::nonNull)
                    .toList();

            if (!temps.isEmpty()) {
                dto.setAvgTemperature(temps.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0));
                dto.setMinTemperature(temps.stream()
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(0.0));
                dto.setMaxTemperature(temps.stream()
                        .mapToDouble(Double::doubleValue)
                        .max()
                        .orElse(0.0));

                // Get last reading time (most recent across all sectors)
                dto.setLastTemperatureReadingTime(sectors.stream()
                        .map(Sector::getLastTemperatureReadingAt)
                        .filter(t -> t != null)
                        .max(LocalDateTime::compareTo)
                        .orElse(null));
            }
        }

        log.info("Current metrics compiled for warehouse {}", warehouseId);
        return dto;
    }

    @Cacheable(value = "temperature_analytics", key = "#warehouseId + '-' + #sectorId + '-' + #granularity")
    public TemperatureAnalyticsDTO getTemperatureAnalytics(Long warehouseId,
                                                           Long sectorId,
                                                           LocalDateTime startDate,
                                                           LocalDateTime endDate,
                                                           String granularity) {
        //log.info("Fetching temperature analytics - Warehouse: {}, Sector: {}, Granularity: {}",
        //        warehouseId, sectorId, granularity);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));

        if (!sector.getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException(
                    "Sector " + sectorId + " does not belong to warehouse " + warehouseId
            );
        }

        TemperatureAnalyticsDTO dto = new TemperatureAnalyticsDTO();
        dto.setWarehouseId(warehouseId);
        dto.setWarehouseName(warehouse.getName());
        dto.setSectorId(sectorId);
        dto.setSectorName(sector.getName());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);
        dto.setDataPoints(new ArrayList<>());

        String flux = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"warehouse_temperature\")\n" +
                        "  |> filter(fn: (r) => r.warehouse_id == \"%d\")\n" +
                        "  |> filter(fn: (r) => r.sector_id == \"%d\")\n" +
                        "  |> filter(fn: (r) => r._field == \"temperature\")\n" +
                        "  |> aggregateWindow(every: %s, fn: mean, createEmpty: false)\n" +
                        "  |> sort(columns: [\"_time\"], desc: false)",
                bucket,
                startDate.atZone(ZoneId.systemDefault()).toInstant(),
                endDate.atZone(ZoneId.systemDefault()).toInstant(),
                warehouseId,
                sectorId,
                granularity
        );

        List<TemperatureAnalyticsDTO.DataPoint> dataPoints = new ArrayList<>();
        List<Double> allTemps = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    TemperatureAnalyticsDTO.DataPoint point = new TemperatureAnalyticsDTO.DataPoint();

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

                    dataPoints.add(point);
                }
            }

            //log.info("Retrieved {} temperature data points for warehouse {} sector {}",
            //        dataPoints.size(), warehouseId, sectorId);

        } catch (Exception e) {
            log.error("Failed to query temperature analytics for warehouse {} sector {}: {}",
                    warehouseId, sectorId, e.getMessage(), e);
        }

        dto.setDataPoints(dataPoints);

        // Calculate statistics
        if (!allTemps.isEmpty()) {
            dto.setAvgTemperature(allTemps.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0));

            dto.setMinTemperature(allTemps.stream()
                    .mapToDouble(Double::doubleValue)
                    .min()
                    .orElse(0.0));

            dto.setMaxTemperature(allTemps.stream()
                    .mapToDouble(Double::doubleValue)
                    .max()
                    .orElse(0.0));
        }

        //log.info("Temperature analytics compiled for warehouse {} sector {}",
        //        warehouseId, sectorId);
        return dto;
    }

    @Cacheable(value = "analytics_availability_warehouse", key = "#warehouseId + '-' + #startDate.toString() + '-' + #endDate.toString() + '-' + #granularity")
    public WarehouseAvailabilityAnalyticsDTO getAvailabilityAnalytics(Long warehouseId,
                                                                      LocalDateTime startDate,
                                                                      LocalDateTime endDate,
                                                                      String granularity) {
//        log.info("Fetching availability analytics - Warehouse: {}, Granularity: {}", warehouseId, granularity);

        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        WarehouseAvailabilityAnalyticsDTO dto = new WarehouseAvailabilityAnalyticsDTO();
        dto.setWarehouseId(warehouseId);
        dto.setWarehouseName(warehouse.getName());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);

        String start = startDate.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endDate.atZone(ZoneId.systemDefault()).toInstant().toString();

        calculateTotalAvailability(dto, warehouseId, start, stop, startDate, endDate);

        List<WarehouseAvailabilityAnalyticsDTO.AvailabilityDataPoint> dataPoints =
                aggregateByTimePeriod(warehouseId, start, stop, granularity);

        dto.setDataPoints(dataPoints);

//        log.info("Availability analytics compiled for warehouse {} - Online: {}%, Offline: {}%, DataPoints: {}",
//                warehouseId, dto.getPercentageOnline(), dto.getPercentageOffline(), dataPoints.size());

        return dto;
    }
    private List<WarehouseAvailabilityAnalyticsDTO.AvailabilityDataPoint> aggregateByTimePeriod(
            Long warehouseId, String start, String stop, String granularity) {

        String fluxForChart = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"warehouse_availability\")\n" +
                        "  |> filter(fn: (r) => r.warehouse_id == \"%d\")\n" +
                        "  |> filter(fn: (r) => r._field == \"online\")\n" +
                        "  |> map(fn: (r) => ({ r with _value: if r._value == true then 1.0 else 0.0 }))\n" +
                        "  |> aggregateWindow(every: %s, fn: mean, createEmpty: false)\n" +
                        "  |> sort(columns: [\"_time\"], desc: false)",
                bucket, start, stop, warehouseId, granularity
        );

        List<WarehouseAvailabilityAnalyticsDTO.AvailabilityDataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxForChart, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    WarehouseAvailabilityAnalyticsDTO.AvailabilityDataPoint point =
                            new WarehouseAvailabilityAnalyticsDTO.AvailabilityDataPoint();

                    Instant instant = record.getTime();
                    if (instant != null) {
                        point.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }

                    Object value = record.getValue();
                    double numericVal = 0.0;

                    if (value instanceof Number) {
                        numericVal = ((Number) value).doubleValue();
                    } else if (value instanceof Boolean) {
                        numericVal = ((Boolean) value) ? 1.0 : 0.0;
                    }

                    double percentage = numericVal * 100.0;
                    point.setUptimePercentage(Math.round(percentage * 10.0) / 10.0);
                    point.setIsOnline(numericVal > 0.5);

                    dataPoints.add(point);
                }
            }
        } catch (Exception e) {
            log.error("Failed to query chart data for warehouse {}: {}", warehouseId, e.getMessage(), e);
        }

        return dataPoints;
    }

    private void calculateTotalAvailability(WarehouseAvailabilityAnalyticsDTO dto,
                                            Long warehouseId, String start, String stop,
                                            LocalDateTime startDate, LocalDateTime endDate) {

        long totalMinutes = ChronoUnit.MINUTES.between(startDate, endDate);

        String fluxForStats = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"warehouse_availability\")\n" +
                        "  |> filter(fn: (r) => r.warehouse_id == \"%d\")\n" +
                        "  |> filter(fn: (r) => r._field == \"online\")\n" +
                        "  |> sort(columns: [\"_time\"], desc: false)",
                bucket, start, stop, warehouseId
        );

        long rawOnlineCount = 0;
        long rawOfflineCount = 0;

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxForStats, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object val = record.getValue();
                    boolean isOnline = false;

                    if (val instanceof Number) {
                        isOnline = ((Number) val).intValue() == 1;
                    } else if (val instanceof Boolean) {
                        isOnline = (Boolean) val;
                    }

                    if (isOnline) {
                        rawOnlineCount++;
                    } else {
                        rawOfflineCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to query stats for warehouse {}: {}", warehouseId, e.getMessage(), e);
        }

        long totalRawCounts = rawOnlineCount + rawOfflineCount;

        if (totalRawCounts > 0) {
            double percentageOnline = (double) rawOnlineCount / totalRawCounts * 100.0;

            dto.setPercentageOnline(Math.round(percentageOnline * 100.0) / 100.0);
            dto.setPercentageOffline(Math.round((100.0 - percentageOnline) * 100.0) / 100.0);

            long calculatedOnlineMinutes = Math.round(totalMinutes * (percentageOnline / 100.0));

            dto.setTotalOnlineMinutes(calculatedOnlineMinutes);
            dto.setTotalOfflineMinutes(totalMinutes - calculatedOnlineMinutes);
        } else {
            dto.setPercentageOnline(0.0);
            dto.setPercentageOffline(0.0);
            dto.setTotalOnlineMinutes(0L);
            dto.setTotalOfflineMinutes(0L);
        }
    }
}
