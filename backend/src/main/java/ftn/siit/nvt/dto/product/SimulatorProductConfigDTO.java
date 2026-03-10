package ftn.siit.nvt.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulatorProductConfigDTO {
    private Long productId;
    private Long minQuantity;
    private Long maxQuantity;
}
