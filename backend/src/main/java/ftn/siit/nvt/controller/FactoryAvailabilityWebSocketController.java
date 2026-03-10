package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import ftn.siit.nvt.service.FactoryRealTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FactoryAvailabilityWebSocketController {

    private final FactoryRealTimeService realTimeService;

    @MessageMapping("/factory/{factoryId}/availability/refresh")
    @SendTo("/topic/factory/{factoryId}/availability")
    public RealTimeAvailabilityDTO refreshAvailability(
            @DestinationVariable Long factoryId) {
        log.info("Manual refresh requested for factory {}", factoryId);
        return realTimeService.getIncrementalUpdate(factoryId);
    }
}
