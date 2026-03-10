package ftn.siit.nvt.dto.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeAvailabilityDTO {
    private Long factoryId;
    private String factoryName;
    private LocalDateTime timestamp;
    private Boolean currentStatus;
    private LocalDateTime lastHeartbeat;

    // Availability metrics for the 3-hour window
    private Double percentageOnline;
    private Double percentageOffline;
    private Long totalOnlineMinutes;
    private Long totalOfflineMinutes;

    // Recent data points (last 3 hours, 1-minute granularity = ~180 points)
    private List<AvailabilityDataPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AvailabilityDataPoint {
        private LocalDateTime timestamp;
        private Boolean isOnline;
        private Double percentageOnline; // For aggregated windows
        private Long onlineMinutes;
        private Long offlineMinutes;
    }
}
