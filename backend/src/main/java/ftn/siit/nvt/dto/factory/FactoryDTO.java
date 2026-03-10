package ftn.siit.nvt.dto.factory;

import ftn.siit.nvt.dto.product.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactoryDTO implements Serializable {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Set<String> imageUrls;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;
    private List<ProductDTO> products;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
