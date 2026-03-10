package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import ftn.siit.nvt.dto.vehicle.VehicleAvailabilityMetricsDTO;
import ftn.siit.nvt.dto.vehicle.VehicleDistanceMetricsDTO;
import ftn.siit.nvt.dto.vehicle.VehicleMetricsDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.DeliveryVehicle;
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
public class DeliveryVehicleAnalyticsService {
    private final DeliveryVehicleRepository vehicleRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    public VehicleMetricsDTO getCurrentMetrics(Long vehicleId) {
        log.info("Fetching current metrics for vehicle: {}", vehicleId);

        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        VehicleMetricsDTO dto = new VehicleMetricsDTO();
        dto.setVehicleId(vehicle.getId());
        dto.setVehicleRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setIsOnline(vehicle.getIsOnline());
        dto.setLastHeartbeat(vehicle.getLastHeartbeat());
        dto.setLastLatitude(vehicle.getLastLatitude());
        dto.setLastLongitude(vehicle.getLastLongitude());
        dto.setLastLocationReadingTime(vehicle.getLastLocationReadingAt());

        log.info("Current metrics compiled for vehicle {}", vehicleId);
        return dto;
    }

    public VehicleAvailabilityMetricsDTO getAvailabilityAnalytics(
            Long vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String granularity) {

        log.info("Fetching availability analytics - Vehicle: {}, Granularity: {}",
                vehicleId, granularity);

        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        VehicleAvailabilityMetricsDTO dto = new VehicleAvailabilityMetricsDTO();
        dto.setVehicleId(vehicleId);
        dto.setVehicleRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);

        String start = startDate.atZone(ZoneId.systemDefault()).toInstant().toString();
        String stop = endDate.atZone(ZoneId.systemDefault()).toInstant().toString();
        calculateTotalAvailability(dto, vehicleId, start, stop, startDate, endDate);

        long totalMinutes = ChronoUnit.MINUTES.between(startDate, endDate);

        calculateTotalAvailability(dto, vehicleId, start, stop, startDate, endDate);

        List<VehicleAvailabilityMetricsDTO.AvailabilityDataPoint> dataPoints =
                aggregateByTimePeriod(vehicleId, start, stop, granularity);

        dto.setDataPoints(dataPoints);

        log.info("Availability analytics compiled for vehicle {} - Online: {}%, Offline: {}%, DataPoints: {}",
                vehicleId, dto.getPercentageOnline(), dto.getPercentageOffline(), dataPoints.size());

        return dto;
    }

    public VehicleDistanceMetricsDTO getDistanceMetrics(
            Long vehicleId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String granularity) {

        log.info("Fetching distance analytics - Vehicle: {}, Granularity: {}",
                vehicleId, granularity);

        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        VehicleDistanceMetricsDTO dto = new VehicleDistanceMetricsDTO();
        dto.setVehicleId(vehicleId);
        dto.setVehicleRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setGranularity(granularity);

        String flux = String.format(
                "from(bucket: \"%s\")\n" +
                        "  |> range(start: %s, stop: %s)\n" +
                        "  |> filter(fn: (r) => r._measurement == \"vehicle_distance\")\n" +
                        "  |> filter(fn: (r) => r.vehicle_id == \"%d\")\n" +
                        "  |> filter(fn: (r) => r._field == \"distance_passed\")\n" +
                        "  |> aggregateWindow(every: %s, fn: sum, createEmpty: false)\n" +
                        "  |> sort(columns: [\"_time\"], desc: false)",
                bucket,
                startDate.atZone(ZoneId.systemDefault()).toInstant(),
                endDate.atZone(ZoneId.systemDefault()).toInstant(),
                vehicleId,
                granularity
        );

        List<VehicleDistanceMetricsDTO.DistanceDataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {

                    VehicleDistanceMetricsDTO.DistanceDataPoint point =
                            new VehicleDistanceMetricsDTO.DistanceDataPoint();

                    Instant instant = record.getTime();
                    if (instant != null) {
                        point.setTimestamp(
                                LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
                        );
                    }

                    Object value = record.getValue();
                    if (value instanceof Number) {
                        point.setDistance(((Number) value).doubleValue());
                    } else {
                        point.setDistance(0.0);
                    }

                    dataPoints.add(point);
                }
            }

        } catch (Exception e) {
            log.error("Failed to query distance analytics for vehicle {}: {}",
                    vehicleId, e.getMessage(), e);
        }

        dto.setDataPoints(dataPoints);

        log.info("Distance analytics compiled for vehicle {}", vehicleId);

        return dto;
    }

    private void calculateTotalAvailability(VehicleAvailabilityMetricsDTO dto,
                                            Long vehicleId,
                                            String start,
                                            String stop,
                                            LocalDateTime startDate,
                                            LocalDateTime endDate) {
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

            log.info("Total availability events for vehicle {}: online={}, offline={}",
                    vehicleId, onlineCount, offlineCount);

        } catch (Exception e) {
            log.error("Failed to query total availability for vehicle {}: {}", vehicleId, e.getMessage(), e);
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

    private List<VehicleAvailabilityMetricsDTO.AvailabilityDataPoint> aggregateByTimePeriod(
            Long vehicleId,
            String start,
            String stop,
            String granularity) {

        String window = mapGranularityToWindow(granularity);
        int minutesInWindow = getMinutesInWindow(granularity);

        log.info("Aggregating availability with window={}, minutesInWindow={}", window, minutesInWindow);

        String flux = String.format("""
            from(bucket: "%s")
              |> range(start: %s, stop: %s)
              |> filter(fn: (r) => r._measurement == "vehicle_availability")
              |> filter(fn: (r) => r.vehicle_id == "%d")
              |> filter(fn: (r) => r._field == "online")
              |> map(fn: (r) => ({ r with _value: if r._value == true then 1.0 else 0.0 }))
              |> aggregateWindow(every: %s, fn: mean, createEmpty: false)
            """, bucket, start, stop, vehicleId, window);

        List<VehicleAvailabilityMetricsDTO.AvailabilityDataPoint> dataPoints = new ArrayList<>();

        try {
            QueryApi queryApi = influxClient.getQueryApi();
            List<FluxTable> tables = queryApi.query(flux, influxOrg);

            for (FluxTable table : tables) {
                for (FluxRecord record : table.getRecords()) {
                    VehicleAvailabilityMetricsDTO.AvailabilityDataPoint point = new VehicleAvailabilityMetricsDTO.AvailabilityDataPoint();

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

            log.info("Aggregated {} data points for vehicle {} with granularity {}",
                    dataPoints.size(), vehicleId, granularity);

        } catch (Exception e) {
            log.error("Failed to aggregate availability for vehicle {}: {}", vehicleId, e.getMessage(), e);
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
