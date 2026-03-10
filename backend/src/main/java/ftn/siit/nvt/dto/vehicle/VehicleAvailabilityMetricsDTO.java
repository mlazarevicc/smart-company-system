package ftn.siit.nvt.dto.vehicle;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleAvailabilityMetricsDTO {
    private Long vehicleId;
    private String vehicleRegistrationNumber;
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
        private Double percentageOnline;
        private Long onlineMinutes;
        private Long offlineMinutes;
    }
}
