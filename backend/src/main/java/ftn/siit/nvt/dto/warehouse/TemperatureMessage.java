package ftn.siit.nvt.dto.warehouse;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TemperatureMessage {
    private Long warehouseId;
    private LocalDateTime timestamp;
    private List<SectorTemperature> temperatures;

    @Data
    public static class SectorTemperature {
        private Long sectorId;
        private Double temperature;
    }
}
