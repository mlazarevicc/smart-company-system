package ftn.siit.nvt.dto.factory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductionMessage {
    private Long factoryId;
    private OffsetDateTime timestamp;
    private List<ProductionDetail> productions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionDetail {
        private Long productId;
        private Long quantity;
    }
}
