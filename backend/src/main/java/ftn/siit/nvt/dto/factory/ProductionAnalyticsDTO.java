package ftn.siit.nvt.dto.factory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductionAnalyticsDTO implements Serializable {

    private Long factoryId;
    private String factoryName;
    private Long productId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity;
    private Long totalProduced;
    private List<DataPoint> dataPoints;

    @Data
    public static class DataPoint implements Serializable {
        private LocalDateTime timestamp;
        private Long quantity;
    }
}
