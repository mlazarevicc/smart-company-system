package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import ftn.siit.nvt.dto.vehicle.RealTimeVehicleAvailabilityDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.DeliveryVehicle;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.repository.DeliveryVehicleRepository;
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
public class DeliveryVehicleRealTimeService {

    private final DeliveryVehicleRepository vehicleRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    private static final int REAL_TIME_WINDOW_HOURS = 3;

    public RealTimeVehicleAvailabilityDTO getInitialAvailabilityData(Long vehicleId) {
        log.info("Fetching initial real-time availability data for factory: {}", vehicleId);

        try {
            DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

            log.info("Vehicle found: {} (online: {})", vehicle.getRegistrationNumber(), vehicle.getIsOnline());

            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusHours(REAL_TIME_WINDOW_HOURS);

            RealTimeVehicleAvailabilityDTO dto = new RealTimeVehicleAvailabilityDTO();
            dto.setVehicleId(vehicleId);
            dto.setVehicleRegistrationNumber(vehicle.getRegistrationNumber());
            dto.setTimestamp(endTime);
            dto.setIsOnline(vehicle.getIsOnline());
            dto.setLastHeartbeat(vehicle.getLastHeartbeat());

            fetchAvailabilityMetrics(dto, vehicleId, startTime, endTime);

            log.info("Successfully fetched initial data - DataPoints: {}, Online: {}%",
                    dto.getDataPoints() != null ? dto.getDataPoints().size() : 0,
                    dto.getPercentageOnline());

            return dto;

        } catch (ResourceNotFoundException e) {
            log.error("Vehicle {} not found in database", vehicleId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to fetch initial data for vehicle {}: {}",
                    vehicleId, e.getMessage(), e);
            throw e;
        }
    }


    public RealTimeVehicleAvailabilityDTO getIncrementalUpdate(Long vehicleId) {
        log.debug("Fetching incremental update for factory: {}", vehicleId);

        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusMinutes(5); // Last 5 minutes

        RealTimeVehicleAvailabilityDTO dto = new RealTimeVehicleAvailabilityDTO();
        dto.setVehicleId(vehicleId);
        dto.setVehicleRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setTimestamp(endTime);
        dto.setIsOnline(vehicle.getIsOnline());
        dto.setLastHeartbeat(vehicle.getLastHeartbeat());

        List<RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint> recentPoints =
                fetchRecentDataPoints(vehicleId, startTime, endTime);
        dto.setDataPoints(recentPoints);

        LocalDateTime fullWindowStart = endTime.minusHours(REAL_TIME_WINDOW_HOURS);
        calculateSummaryMetrics(dto, vehicleId, fullWindowStart, endTime);

        return dto;
    }

    private void fetchAvailabilityMetrics(RealTimeVehicleAvailabilityDTO dto,
                                          Long vehicleId,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime) {
        List<RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint> dataPoints =
                fetchRecentDataPoints(vehicleId, startTime, endTime);
        dto.setDataPoints(dataPoints);

        calculateSummaryMetrics(dto, vehicleId, startTime, endTime);
    }

    private List<RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint> fetchRecentDataPoints(
            Long vehicleId, LocalDateTime startTime, LocalDateTime endTime) {

        String start = startTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endTime.atZone(ZoneId.systemDefault()).toInstant().toString();

        // Query with 1-minute aggregation for smooth real-time charts
        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "vehicle_availability")
              |> filter(fn: (r) => r.vehicle_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> map(fn: (r) => ({ r with _value: if r._value == true then 1.0 else 0.0 }))
              |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)
              |> sort(columns: ["_time"], desc: false)
            """, bucket, start, stop, vehicleId);

        List<RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint point =
                            new RealTimeVehicleAvailabilityDTO.AvailabilityDataPoint();

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

            log.info("Fetched {} real-time data points for vehicle {}", dataPoints.size(), vehicleId);
        } catch (Exception e) {
            log.error("Failed to fetch real-time data points for vehicle {}: {}",
                    vehicleId, e.getMessage(), e);
        }

        return dataPoints;
    }

    private void calculateSummaryMetrics(RealTimeVehicleAvailabilityDTO dto,
                                         Long vehicleId,
                                         LocalDateTime startTime,
                                         LocalDateTime endTime) {
        String start = startTime.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endTime.atZone(ZoneId.systemDefault()).toInstant().toString();

        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "vehicle_availability")
              |> filter(fn: (r) => r.vehicle_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> sort(columns: ["_time"], desc: false)
            """, bucket, start, stop, vehicleId);

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
        } catch (Exception e) {
            log.error("Failed to calculate summary metrics for vehicle {}: {}",
                    vehicleId, e.getMessage(), e);
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
