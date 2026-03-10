package ftn.siit.nvt.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeTemperatureDTO {
    private Long warehouseId;
    private String warehouseName;
    private LocalDateTime timestamp;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;
    private Double avgTemperature;
    private Double minTemperature;
    private Double maxTemperature;
    private List<SectorTemperatureData> sectorData;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SectorTemperatureData {
        private Long sectorId;
        private String sectorName;
        private Double currentTemperature;
        private LocalDateTime lastReading;
        private List<TemperatureDataPoint> dataPoints;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemperatureDataPoint {
        private LocalDateTime timestamp;
        private Double temperature;
    }
}
