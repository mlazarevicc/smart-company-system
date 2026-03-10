package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ftn.siit.nvt.dto.factory.ProductionAnalyticsDTO;
import ftn.siit.nvt.dto.factory.AvailabilityAnalyticsDTO;
import ftn.siit.nvt.dto.factory.FactoryMetricsDTO;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.repository.FactoryRepository;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryAnalyticsService {

    private final FactoryRepository factoryRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    @Cacheable(value = "analytics_production", key = "#factoryId + '-' + #productId + '-' + #startDate.toString() + '-' + #endDate.toString() + '-' + #granularity")
    public ProductionAnalyticsDTO getProductionAnalytics(Long factoryId,
                                                         Long productId,
                                                         LocalDateTime startDate,
                                                         LocalDateTime endDate,
                                                         String granularity) {
        log.info("Fetching production analytics - Factory: {}, Product: {}, Granularity: {}",
                factoryId, productId, granularity);

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

        ProductionAnalyticsDTO dto = new ProductionAnalyticsDTO();
        dto.setFactoryId(factoryId);
        dto.setFactoryName(factory.getName());
        dto.setProductId(productId);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);

        String window = mapGranularityToWindow(granularity);
        String start = startDate.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endDate.atZone(ZoneId.systemDefault()).toInstant().toString();

        StringBuilder flux = new StringBuilder(String.format("""
        from(bucket: "%s")
          |> range(start: %s, stop: %s)
          |> filter(fn: (r) => r["_measurement"] == "factory_production")
          |> filter(fn: (r) => r["factory_id"] == "%d")
          |> filter(fn: (r) => r["_field"] == "quantity")
        """, bucket, start, stop, factoryId));

        if (productId != null) {
            flux.append(String.format("""
          |> filter(fn: (r) => r["product_id"] == "%d")
        """, productId));
        }

        flux.append(String.format("""
          |> aggregateWindow(every: %s, fn: sum, createEmpty: false)
          |> keep(columns: ["_time", "_value"])
          |> sort(columns: ["_time"], desc: false)
        """, window));

        List<ProductionAnalyticsDTO.DataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux.toString(), influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    ProductionAnalyticsDTO.DataPoint point = new ProductionAnalyticsDTO.DataPoint();

                    Instant instant = record.getTime();
                    if (instant != null) {
                        point.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }

                    Object value = record.getValue();
                    if (value instanceof Number) {
                        point.setQuantity(((Number) value).longValue());
                    } else {
                        point.setQuantity(0L);
                    }

                    if (point.getTimestamp() != null) {
                        dataPoints.add(point);
                    }
                }
            }

            log.info("Retrieved {} production data points for factory {}", dataPoints.size(), factoryId);

        } catch (Exception e) {
            log.error("Failed to query production analytics for factory {}: {}", factoryId, e.getMessage(), e);
        }

        dto.setDataPoints(dataPoints);

        long totalProduced = dataPoints.stream()
                .mapToLong(p -> p.getQuantity() != null ? p.getQuantity() : 0L)
                .sum();

        dto.setTotalProduced(totalProduced);

        log.info("Production analytics compiled for factory {} - Total: {}", factoryId, dto.getTotalProduced());

        return dto;
    }

    @Cacheable(value = "analytics_availability", key = "#factoryId + '-' + #startDate.toString() + '-' + #endDate.toString() + '-' + #granularity")
    public AvailabilityAnalyticsDTO getAvailabilityAnalytics(Long factoryId,
                                                             LocalDateTime startDate,
                                                             LocalDateTime endDate,
                                                             String granularity) {
        log.info("Fetching availability analytics - Factory: {}, Granularity: {}", factoryId, granularity);

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

        AvailabilityAnalyticsDTO dto = new AvailabilityAnalyticsDTO();
        dto.setFactoryId(factoryId);
        dto.setFactoryName(factory.getName());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);

        String start = startDate.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endDate.atZone(ZoneId.systemDefault()).toInstant().toString();

        calculateTotalAvailability(dto, factoryId, start, stop, startDate, endDate);

        List<AvailabilityAnalyticsDTO.DataPoint> dataPoints =
                aggregateByTimePeriod(factoryId, start, stop, granularity);

        dto.setDataPoints(dataPoints);

        log.info("Availability analytics compiled for factory {} - Online: {}%, Offline: {}%, DataPoints: {}",
                factoryId, dto.getPercentageOnline(), dto.getPercentageOffline(), dataPoints.size());

        return dto;
    }

    public FactoryMetricsDTO getCurrentMetrics(Long factoryId) {
        log.info("Fetching current metrics for factory: {}", factoryId);

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

        FactoryMetricsDTO dto = new FactoryMetricsDTO();
        dto.setFactoryId(factoryId);
        dto.setFactoryName(factory.getName());
        dto.setIsOnline(factory.getIsOnline());
        dto.setLastHeartbeat(factory.getLastHeartbeat());

        String fluxProduction = String.format("""
            from(bucket: "%s")
              |> range(start: -1h)
              |> filter(fn: (r) => r._measurement == "factory_production")
              |> filter(fn: (r) => r.factory_id == "%d")
              |> filter(fn: (r) => r._field == "quantity")
              |> last()
            """, bucket, factoryId);

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(fluxProduction, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object value = record.getValue();
                    if (value instanceof Number) {
                        dto.setLastProductionQuantity(((Number) value).longValue());
                    }

                    Instant instant = record.getTime();
                    if (instant != null) {
                        dto.setLastProductionTime(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to query current metrics for factory {}: {}", factoryId, e.getMessage(), e);
        }

        log.info("Current metrics compiled for factory {}", factoryId);

        return dto;
    }

    private void calculateTotalAvailability(AvailabilityAnalyticsDTO dto,
                                            Long factoryId,
                                            String start,
                                            String stop,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate) {
        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "factory_availability")
              |> filter(fn: (r) => r.factory_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> sort(columns: ["_time"], desc: false)
            """, bucket, start, stop, factoryId);

        long onlineCount = 0;
        long offlineCount = 0;

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    Object value = record.getValue();
                    boolean isOnline = false;

                    if (value instanceof Boolean) {
                        isOnline = (Boolean) value;
                    } else if (value instanceof Number) {
                        isOnline = ((Number) value).intValue() == 1;
                    }

                    if (isOnline) {
                        onlineCount++;
                    } else {
                        offlineCount++;
                    }
                }
            }

            log.info("Total availability events for factory {}: online={}, offline={}",
                    factoryId, onlineCount, offlineCount);

        } catch (Exception e) {
            log.error("Failed to query total availability for factory {}: {}", factoryId, e.getMessage(), e);
        }

        long totalMinutes = ChronoUnit.MINUTES.between(startDate, endDate);
        long totalEvents = onlineCount + offlineCount;

        if (totalEvents > 0) {
            long estimatedOnlineMinutes = (onlineCount * totalMinutes) / totalEvents;
            long estimatedOfflineMinutes = totalMinutes - estimatedOnlineMinutes;

            dto.setTotalOnlineMinutes(estimatedOnlineMinutes);
            dto.setTotalOfflineMinutes(estimatedOfflineMinutes);

            double percentOnline = (estimatedOnlineMinutes * 100.0) / totalMinutes;
            dto.setPercentageOnline(Math.round(percentOnline * 100.0) / 100.0);
            dto.setPercentageOffline(Math.round((100.0 - percentOnline) * 100.0) / 100.0);
        } else {
            dto.setTotalOnlineMinutes(0L);
            dto.setTotalOfflineMinutes(totalMinutes);
            dto.setPercentageOnline(0.0);
            dto.setPercentageOffline(100.0);
        }
    }

    private List<AvailabilityAnalyticsDTO.DataPoint> aggregateByTimePeriod(
            Long factoryId,
            String start,
            String stop,
            String granularity) {

        String window = mapGranularityToWindow(granularity);
        int minutesInWindow = getMinutesInWindow(granularity);

        log.info("Aggregating availability with window={}, minutesInWindow={}", window, minutesInWindow);

        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "factory_availability")
              |> filter(fn: (r) => r.factory_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> map(fn: (r) => ({ r with _value: if r._value == true then 1.0 else 0.0 }))
              |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
            """, bucket, start, stop, factoryId, window);

        List<AvailabilityAnalyticsDTO.DataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    AvailabilityAnalyticsDTO.DataPoint point = new AvailabilityAnalyticsDTO.DataPoint();

                    Instant instant = record.getTime();
                    if (instant != null) {
                        point.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }

                    Object value = record.getValue();
                    double avgOnline = 0.0;

                    if (value instanceof Number) {
                        avgOnline = ((Number) value).doubleValue();
                    }

                    long onlineMinutes = Math.round(avgOnline * minutesInWindow);
                    long offlineMinutes = minutesInWindow - onlineMinutes;
                    double percentageOnline = avgOnline * 100.0;

                    point.setIsOnline(null);
                    point.setPercentageOnline(Math.round(percentageOnline * 100.0) / 100.0);
                    point.setOnlineMinutes(onlineMinutes);
                    point.setOfflineMinutes(offlineMinutes);

                    dataPoints.add(point);
                }
            }

            log.info("Aggregated {} data points for factory {} with granularity {}",
                    dataPoints.size(), factoryId, granularity);

        } catch (Exception e) {
            log.error("Failed to aggregate availability for factory {}: {}", factoryId, e.getMessage(), e);
        }

        return dataPoints;
    }

    private String mapGranularityToWindow(String granularity) {
        return switch (granularity.toLowerCase()) {
            case "minute", "1m" -> "1m";
            case "5minutes", "5m" -> "5m";
            case "15minutes", "15m" -> "15m";
            case "hour", "1h" -> "1h";
            case "day", "1d" -> "1d";
            case "week", "1w" -> "1w";
            case "month", "1mo" -> "1mo";
            default -> {
                log.warn("Unknown granularity: {}, defaulting to 1d", granularity);
                yield "1d";
            }
        };
    }

    private int getMinutesInWindow(String granularity) {
        return switch (granularity.toLowerCase()) {
            case "minute", "1m" -> 1;
            case "5minutes", "5m" -> 5;
            case "15minutes", "15m" -> 15;
            case "hour", "1h" -> 60;
            case "day", "1d" -> 1440;
            case "week", "1w" -> 10080;
            case "month", "1mo" -> 43200;
            default -> {
                log.warn("Unknown granularity: {}, defaulting to 1440 minutes", granularity);
                yield 1440;
            }
        };
    }
}
