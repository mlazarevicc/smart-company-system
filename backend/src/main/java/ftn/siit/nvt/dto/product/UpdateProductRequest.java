package ftn.siit.nvt.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import ftn.siit.nvt.model.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class UpdateProductRequest {

    @NotNull(message = "Version is required (for optimistic locking)")
    private Long version;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255)
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01")
    private BigDecimal weight;

    private String imageUrl;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    private Set<Long> factoryIds;
}
