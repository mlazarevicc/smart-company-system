package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.repository.FactoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class FactoryRealTimeService {

    private final FactoryRepository factoryRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    private static final int REAL_TIME_WINDOW_HOURS = 3;

    public RealTimeAvailabilityDTO getInitialAvailabilityData(Long factoryId) {
        log.info("Fetching initial real-time availability data for factory: {}", factoryId);

        try {
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

            log.info("Factory found: {} (online: {})", factory.getName(), factory.getIsOnline());

            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(REAL_TIME_WINDOW_HOURS);

            RealTimeAvailabilityDTO dto = new RealTimeAvailabilityDTO();
            dto.setFactoryId(factoryId);
            dto.setFactoryName(factory.getName());
            dto.setTimestamp(endTime);
            dto.setCurrentStatus(factory.getIsOnline());
            dto.setLastHeartbeat(factory.getLastHeartbeat());

            fetchAvailabilityMetrics(dto, factoryId, startTime, endTime);

            log.info("Successfully fetched initial data - DataPoints: {}, Online: {}%",
                    dto.getDataPoints() != null ? dto.getDataPoints().size() : 0,
                    dto.getPercentageOnline());

            return dto;

        } catch (ResourceNotFoundException e) {
            log.error("Factory {} not found in database", factoryId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch initial data for factory {}: {}",
                    factoryId, e.getMessage(), e);
            throw e;
        }
    }


    public RealTimeAvailabilityDTO getIncrementalUpdate(Long factoryId) {
        log.debug("Fetching incremental update for factory: {}", factoryId);

        Factory factory = factoryRepository.findById(factoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5); // Last 5 minutes

        RealTimeAvailabilityDTO dto = new RealTimeAvailabilityDTO();
        dto.setFactoryId(factoryId);
        dto.setFactoryName(factory.getName());
        dto.setTimestamp(endTime);
        dto.setCurrentStatus(factory.getIsOnline());
        dto.setLastHeartbeat(factory.getLastHeartbeat());

        List<RealTimeAvailabilityDTO.AvailabilityDataPoint> recentPoints =
                fetchRecentDataPoints(factoryId, startTime, endTime);
        dto.setDataPoints(recentPoints);

        LocalDateTime fullWindowStart = endTime.minusHours(REAL_TIME_WINDOW_HOURS);
        calculateSummaryMetrics(dto, factoryId, fullWindowStart, endTime);

        return dto;
    }

    private void fetchAvailabilityMetrics(RealTimeAvailabilityDTO dto,
                                          Long factoryId,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime) {
        List<RealTimeAvailabilityDTO.AvailabilityDataPoint> dataPoints =
                fetchRecentDataPoints(factoryId, startTime, endTime);
        dto.setDataPoints(dataPoints);

        calculateSummaryMetrics(dto, factoryId, startTime, endTime);
    }

    private List<RealTimeAvailabilityDTO.AvailabilityDataPoint> fetchRecentDataPoints(
            Long factoryId, LocalDateTime startTime, LocalDateTime endTime) {

        String start = startTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endTime.atZone(ZoneId.systemDefault()).toInstant().toString();

        // Query with 1-minute aggregation for smooth real-time charts
        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "factory_availability")
              |> filter(fn: (r) => r.factory_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> map(fn: (r) => ({ r with _value: if r._value == true then 1.0 else 0.0 }))
              |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)
              |> sort(columns: ["_time"], desc: false)
            """, bucket, start, stop, factoryId);

        List<RealTimeAvailabilityDTO.AvailabilityDataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    RealTimeAvailabilityDTO.AvailabilityDataPoint point =
                            new RealTimeAvailabilityDTO.AvailabilityDataPoint();

                    Instant instant = record.getTime();
                    if (instant != null) {
                        point.setTimestamp(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                    }

                    Object value = record.getValue();
                    if (value instanceof Number) {
                        double avgOnline = ((Number) value).doubleValue();
                        point.setPercentageOnline(Math.round(avgOnline * 10000.0) / 100.0);
                        point.setIsOnline(avgOnline > 0.5);
                        point.setOnlineMinutes((long) Math.round(avgOnline));
                        point.setOfflineMinutes(1L - point.getOnlineMinutes());
                    }

                    dataPoints.add(point);
                }
            }

            log.info("Fetched {} real-time data points for factory {}", dataPoints.size(), factoryId);
        } catch (Exception e) {
            log.error("Failed to fetch real-time data points for factory {}: {}",
                    factoryId, e.getMessage(), e);
        }

        return dataPoints;
    }

    private void calculateSummaryMetrics(RealTimeAvailabilityDTO dto,
                                         Long factoryId,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime) {
        String start = startTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endTime.atZone(ZoneId.systemDefault()).toInstant().toString();

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

            log.info("ONLINE COUNT: {} , OFFLINE COUNT: {}", onlineCount,offlineCount);
        } catch (Exception e) {
            log.error("Failed to calculate summary metrics for factory {}: {}",
                    factoryId, e.getMessage(), e);
        }

        long totalMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
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
}
