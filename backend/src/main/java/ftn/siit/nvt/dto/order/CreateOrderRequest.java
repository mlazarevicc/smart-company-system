package ftn.siit.nvt.dto.order;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItemRequest> items;
}
