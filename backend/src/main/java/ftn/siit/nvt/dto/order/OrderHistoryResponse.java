package ftn.siit.nvt.dto.order;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderHistoryResponse {
    private Long id;
    private LocalDateTime orderDate;
    private String deliveryCompanyName;
    private BigDecimal totalPrice;
    private List<OrderItemHistory> items;

    @Data
    public static class OrderItemHistory {
        private String productName;
        private Long quantity;
        private BigDecimal price;
    }
}
