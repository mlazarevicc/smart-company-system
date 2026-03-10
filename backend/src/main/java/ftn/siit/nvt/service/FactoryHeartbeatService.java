package ftn.siit.nvt.service;

import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.repository.FactoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryHeartbeatService {

    private static final int HEARTBEAT_TIMEOUT_SECONDS = 60;

    private final FactoryRepository factoryRepository;
    private final FactoryProductionService productionService;

    @Scheduled(fixedDelay = 30000) // Check every 30 seconds
    @Transactional
    public void checkFactoryAvailability() {
        log.debug("Checking factory heartbeat availability");

        LocalDateTime threshold = LocalDateTime.now().minusSeconds(HEARTBEAT_TIMEOUT_SECONDS);

        List<Factory> offlineCandidates = factoryRepository.findByIsOnlineFalse();

        offlineCandidates.addAll(factoryRepository.findAll()
                .stream()
                .filter(f -> f.getIsOnline() &&
                        (f.getLastHeartbeat() == null || f.getLastHeartbeat().isBefore(threshold)))
                .toList());

        for (Factory factory : offlineCandidates) {
            boolean justWentOffline = factory.getIsOnline();

            if (justWentOffline) {
                log.warn("Factory {} is marked as offline (heartbeat timeout)", factory.getId());
                factory.setIsOnline(false);
                factoryRepository.save(factory);
            }

            productionService.recordAvailability(
                    factory.getId(),
                    false,
                    LocalDateTime.now(),
                    justWentOffline ? "heartbeat_missed" : "offline"
            );
        }
    }
}
