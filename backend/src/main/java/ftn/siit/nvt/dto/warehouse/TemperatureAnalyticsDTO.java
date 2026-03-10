package ftn.siit.nvt.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureAnalyticsDTO {
    private Long warehouseId;
    private String warehouseName;
    private Long sectorId;
    private String sectorName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity;

    // Statistics
    private Double avgTemperature;
    private Double minTemperature;
    private Double maxTemperature;

    private List<DataPoint> dataPoints;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataPoint {
        private LocalDateTime timestamp;
        private Double temperature;
    }
}
