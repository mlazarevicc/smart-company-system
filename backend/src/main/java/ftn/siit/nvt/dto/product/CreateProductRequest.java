package ftn.siit.nvt.dto.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import ftn.siit.nvt.model.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateProductRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be > 0")
    private BigDecimal price;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be > 0")
    private BigDecimal weight;

    @JsonProperty("is_available")
    private Boolean isAvailable = true;

    private Set<Long> factoryIds;
}
