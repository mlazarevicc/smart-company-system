package ftn.siit.nvt.dto.warehouse;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SectorDTO {
    private Long id;
    private String name;
    private String description;
    private Double lastTemperature;
    private LocalDateTime lastTemperatureReadingAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
    private Long warehouseId;
    private String warehouseName;
}
