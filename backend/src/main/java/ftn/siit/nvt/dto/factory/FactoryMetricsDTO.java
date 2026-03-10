package ftn.siit.nvt.dto.factory;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FactoryMetricsDTO {

    private Long factoryId;
    private String factoryName;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;
    private Long lastProductionQuantity;
    private LocalDateTime lastProductionTime;
}
