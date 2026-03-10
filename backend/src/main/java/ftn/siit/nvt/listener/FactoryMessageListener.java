package ftn.siit.nvt.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import ftn.siit.nvt.dto.factory.HeartbeatMessage;
import ftn.siit.nvt.dto.factory.ProductionMessage;
import ftn.siit.nvt.service.FactoryService;
import ftn.siit.nvt.service.FactoryProductionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryMessageListener {

    private final FactoryService factoryService;
    private final FactoryProductionService productionService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "factory-heartbeat")
    public void handleHeartbeat(String message) {
        log.debug("Received heartbeat message: {}", message);
        try {
            HeartbeatMessage heartbeat = objectMapper.readValue(message, HeartbeatMessage.class);

            factoryService.recordHeartbeat(heartbeat.getFactoryId());

            boolean isOnline = "online".equalsIgnoreCase(heartbeat.getStatus());
            productionService.recordAvailability(
                    heartbeat.getFactoryId(),
                    isOnline,
                    heartbeat.getTimestamp().toLocalDateTime(),
                    heartbeat.getStatus() != null ? heartbeat.getStatus() : "heartbeat_received"
            );

            log.info("Heartbeat processed successfully for factory {} - Status: {}",
                    heartbeat.getFactoryId(), heartbeat.getStatus());
        } catch (Exception e) {
            log.error("Error processing heartbeat: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "factory-production-updates")
    public void handleProductionUpdate(String message) {
        log.info("Received production update message: {}", message);
        try {
            ProductionMessage productionMsg = objectMapper.readValue(message, ProductionMessage.class);

            for (ProductionMessage.ProductionDetail detail : productionMsg.getProductions()) {
                productionService.recordProduction(
                        productionMsg.getFactoryId(),
                        productionMsg.getTimestamp().toLocalDateTime(),
                        detail.getProductId(),
                        detail.getQuantity()
                );
            }

            log.info("Production update processed successfully for factory {}", productionMsg.getFactoryId());
        } catch (Exception e) {
            log.error("Error processing production update: {}", e.getMessage(), e);
        }
    }

}
