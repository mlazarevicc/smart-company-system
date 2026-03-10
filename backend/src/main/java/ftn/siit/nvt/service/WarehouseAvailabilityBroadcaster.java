package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.warehouse.RealTimeTemperatureDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseAvailabilityBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final WarehouseRealTimeService warehouseRealTimeService;

    // Map: warehouseId -> Set of sessionIds
    private final Map<Long, Set<String>> warehouseSubscriptions = new ConcurrentHashMap<>();

    public void subscribeToWarehouse(Long warehouseId, String sessionId) {
        warehouseSubscriptions
                .computeIfAbsent(warehouseId, k -> new CopyOnWriteArraySet<>())
                .add(sessionId);

        log.info("Session {} subscribed to warehouse {} (total subscribers: {})",
                sessionId, warehouseId, warehouseSubscriptions.get(warehouseId).size());
    }

    public void unsubscribe(String sessionId) {
        warehouseSubscriptions.values().forEach(sessions -> sessions.remove(sessionId));
        log.info("Session {} unsubscribed from all warehouses", sessionId);
    }

    public void broadcastTemperatureUpdate(Long warehouseId) {
        Set<String> subscribers = warehouseSubscriptions.get(warehouseId);

        if (subscribers == null || subscribers.isEmpty()) {
            log.debug("No subscribers for warehouse {}, skipping broadcast", warehouseId);
            return;
        }

        try {
            RealTimeTemperatureDTO data = warehouseRealTimeService.getInitialTemperatureData(warehouseId);
            String destination = "/topic/warehouse/" + warehouseId + "/temperature";

            messagingTemplate.convertAndSend(destination, data);

            log.info("Broadcasted temperature update for warehouse {} to {} subscribers",
                    warehouseId, subscribers.size());

        } catch (Exception e) {
            log.error("Failed to broadcast temperature update for warehouse {}: {}",
                    warehouseId, e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void broadcastPeriodicUpdates() {
        if (warehouseSubscriptions.isEmpty()) {
            return;
        }
        log.debug("Broadcasting periodic updates to {} warehouses", warehouseSubscriptions.size());
        warehouseSubscriptions.keySet().forEach(this::broadcastTemperatureUpdate);
    }
}
