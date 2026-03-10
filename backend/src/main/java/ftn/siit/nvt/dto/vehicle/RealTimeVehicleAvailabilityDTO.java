package ftn.siit.nvt.dto.vehicle;

import ftn.siit.nvt.dto.factory.RealTimeAvailabilityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeVehicleAvailabilityDTO {
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private LocalDateTime timestamp;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;

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
