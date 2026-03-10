package ftn.siit.nvt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.siit.nvt.dto.vehicle.DistanceMessage;
import ftn.siit.nvt.dto.vehicle.HeartbeatMessage;
import ftn.siit.nvt.service.DeliveryVehicleDistanceService;
import ftn.siit.nvt.service.DeliveryVehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleMessageListener {
    private final DeliveryVehicleService vehicleService;
    private final DeliveryVehicleDistanceService vehicleDistanceService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "vehicle-heartbeat")
    public void handleHeartbeat(String message) {
        log.debug("Received heartbeat message: {}", message);
        try {
            ftn.siit.nvt.dto.vehicle.HeartbeatMessage heartbeat = objectMapper.readValue(message, HeartbeatMessage.class);

            vehicleService.recordHeartbeat(heartbeat.getVehicleId());

            boolean isOnline = "online".equalsIgnoreCase(heartbeat.getStatus());
            vehicleDistanceService.recordAvailability(
                    heartbeat.getVehicleId(),
                    isOnline,
                    heartbeat.getTimestamp(),
                    heartbeat.getStatus() != null ? heartbeat.getStatus() : "heartbeat_received"
            );

            log.info("Heartbeat processed successfully for vehicle {} - Status: {}",
                    heartbeat.getVehicleId(), heartbeat.getStatus());
        } catch (Exception e) {
            log.error("Error processing heartbeat: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "vehicle-distance-updates")
    public void handleDistanceUpdate(String message) {
        log.info("Received distance update message: {}", message);
        try {
            DistanceMessage distanceMessage = objectMapper.readValue(message, DistanceMessage.class);

            vehicleDistanceService.recordDistance(
                    distanceMessage.getVehicleId(),
                    distanceMessage.getTimestamp(),
                    distanceMessage.getDistance(),
                    distanceMessage.getLatitude(),
                    distanceMessage.getLongitude()
            );

            log.info("Distance update processed successfully for vehicle {}", distanceMessage.getVehicleId());
        } catch (Exception e) {
            log.error("Error processing distance update: {}", e.getMessage(), e);
        }
    }
}
