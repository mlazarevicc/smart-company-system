package ftn.siit.nvt.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ftn.siit.nvt.dto.factory.FactorySimpleDTO;
import ftn.siit.nvt.model.enums.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
//@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class ProductDTO implements Serializable {

    private Long id;

    private String sku;

    private String name;

    private String description;

    private ProductCategory category;

    private BigDecimal price;

    private BigDecimal weight;

    private Long totalQuantity;

    @JsonProperty("product_image")
    private String productImage;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("is_on_sale")
    private Boolean isOnSale;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    private Long version;

    @JsonProperty(value = "category_name", access = JsonProperty.Access.READ_ONLY)
    public String getCategoryName() {
        return category != null ? category.getDisplayName() : "Unknown";
    }

    @JsonProperty(value = "display_price", access = JsonProperty.Access.READ_ONLY)
    public String getDisplayPrice() {
        return price != null ? price.toPlainString() + " EUR" : "N/A";
    }

    private Set<Long> factoryIds;

    private List<FactorySimpleDTO> factories;
}