package ftn.siit.nvt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryVehicleAvailabilityBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final DeliveryVehicleRealTimeService vehicleRealTimeService;

    private final Map<Long, Set<String>> vehicleSubscriptions = new ConcurrentHashMap<>();

    public void subscribeToVehicle(Long vehicleId, String sessionId) {
        vehicleSubscriptions
                .computeIfAbsent(vehicleId, k -> new CopyOnWriteArraySet<>())
                .add(sessionId);

        log.info("Session {} subscribed to vehicle {} (Active vehicles: {})",
                sessionId, vehicleId, vehicleSubscriptions.size());
    }
}
