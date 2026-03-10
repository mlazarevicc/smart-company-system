package ftn.siit.nvt.dto.factory;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityAnalyticsDTO implements Serializable {
    private Long factoryId;
    private String factoryName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity;
    private Double percentageOnline;
    private Double percentageOffline;
    private Long totalOnlineMinutes;
    private Long totalOfflineMinutes;
    private List<DataPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint implements Serializable{
        private LocalDateTime timestamp;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Boolean isOnline;
        private Double percentageOnline;
        private Long onlineMinutes;
        private Long offlineMinutes;
    }
}
