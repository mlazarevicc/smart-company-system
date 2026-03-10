package ftn.siit.nvt.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseMetricsDTO {
    private Long warehouseId;
    private String warehouseName;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;
    private Integer totalSectors;
    private Double avgTemperature;
    private Double minTemperature;
    private Double maxTemperature;
    private LocalDateTime lastTemperatureReadingTime;

    private Double lastRecordedTemperature;
    private LocalDateTime lastInfluxReadingTime;
}
