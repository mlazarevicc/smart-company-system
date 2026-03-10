package ftn.siit.nvt.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorWarehouseConfigDTO {
    private Long id;
    private String name;
    private List<SimulatorSectorConfigDTO> sectors;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulatorSectorConfigDTO {
        private Long id;
        private String name;
    }
}
