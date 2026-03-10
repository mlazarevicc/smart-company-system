package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryAvailabilityBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final FactoryRealTimeService realTimeService;
    
    private final Set<Long> activeFactories = ConcurrentHashMap.newKeySet();
    private final Map<String, Long> sessionToFactory = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 10000)
    public void broadcastAvailabilityUpdates() {
        if (activeFactories.isEmpty()) {
            log.debug("No active subscriptions, skipping broadcast");
            return;
        }

        log.info("Broadcasting availability updates for {} factories", activeFactories.size());

        for (Long factoryId : activeFactories) {
            try {
                log.debug("Fetching update for factory {}", factoryId);
                RealTimeAvailabilityDTO update = realTimeService.getIncrementalUpdate(factoryId);

                String destination = "/topic/factory/" + factoryId + "/availability";
                log.debug("Sending to: {}", destination);

                messagingTemplate.convertAndSend(destination, update);

                log.info("Broadcast successful for factory {}", factoryId);
            } catch (Exception e) {
                log.error("Failed to broadcast update for factory {}: {}",
                        factoryId, e.getMessage(), e);
            }
        }
    }

    public void subscribeToFactory(Long factoryId, String sessionId) {
        activeFactories.add(factoryId);
        sessionToFactory.put(sessionId, factoryId);

        log.info("Factory {} added to broadcast list (session: {}). Active factories: {}",
                factoryId, sessionId, activeFactories.size());
    }

    public void handleSessionDisconnect(String sessionId) {
        Long factoryId = sessionToFactory.remove(sessionId);

        if (factoryId != null) {
            log.info("Session {} disconnected (was subscribed to factory {})",
                    sessionId, factoryId);

            // Check if any other sessions are still subscribed
            boolean hasOtherSessions = sessionToFactory.containsValue(factoryId);

            if (!hasOtherSessions) {
                activeFactories.remove(factoryId);
                log.info("Factory {} removed from broadcast list (no more sessions). Active: {}",
                        factoryId, activeFactories.size());
            }
        }
    }
}
