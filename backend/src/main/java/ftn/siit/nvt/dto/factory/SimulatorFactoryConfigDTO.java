package ftn.siit.nvt.dto.factory;

import ftn.siit.nvt.dto.product.SimulatorProductConfigDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorFactoryConfigDTO {
    private Long id;
    private String name;
    private List<SimulatorProductConfigDTO> products;
}
