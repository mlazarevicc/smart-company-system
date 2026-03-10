package ftn.siit.nvt.dto.warehouse;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseAvailabilityAnalyticsDTO {
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity;

    private Double percentageOnline;
    private Double percentageOffline;
    private Long totalOnlineMinutes;
    private Long totalOfflineMinutes;

    private List<AvailabilityDataPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityDataPoint {
        private LocalDateTime timestamp;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean isOnline;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Double uptimePercentage;
    }
}
