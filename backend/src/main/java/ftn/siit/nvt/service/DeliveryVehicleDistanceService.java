package ftn.siit.nvt.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.DeliveryVehicle;
import ftn.siit.nvt.model.Sector;
import ftn.siit.nvt.model.Warehouse;
import ftn.siit.nvt.repository.DeliveryVehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryVehicleDistanceService {
    private final DeliveryVehicleRepository vehicleRepository;
    private final InfluxDBClient influxClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String influxOrg;

    @Transactional
    public void recordDistance(Long vehicleId, OffsetDateTime timestamp, Double distance, Double latitude, Double longitude) {
        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        vehicle.setLastLatitude(latitude);
        vehicle.setLastLongitude(longitude);

        vehicle.setLastLocationReadingAt(timestamp.toLocalDateTime());
        vehicleRepository.save(vehicle);

        Point point = Point
                .measurement("vehicle_distance")
                .addTag("vehicle_id", vehicleId.toString())
                .addField("distance_passed", distance)
                .time(timestamp.toInstant(), WritePrecision.MS);

        // 3. Upiši u InfluxDB
        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
            log.info("Distance passed recorded in InfluxDB: Vehicle {}, Distance: {}",
                    vehicleId, distance);
        } catch (Exception e) {
            log.error("Failed to write distance passed to Influx for vehicle {}",
                    vehicleId, e);
        }
    }

    public void recordAvailability(Long vehicleId,
                                   boolean isOnline,
                                   OffsetDateTime timestamp,
                                   String reason) {
        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        Point point = Point
                .measurement("vehicle_availability")
                .addTag("vehicle_id", vehicleId.toString())
                .addField("online", isOnline)
                .addField("reason", reason)
                .time(timestamp.toInstant(), WritePrecision.MS);

        try {
            influxClient.getWriteApiBlocking().writePoint(bucket, influxOrg, point);
        } catch (Exception e) {
            log.error("Failed to write availability metrics to Influx for vehicle {}",
                    vehicle, e);
        }
    }
}
