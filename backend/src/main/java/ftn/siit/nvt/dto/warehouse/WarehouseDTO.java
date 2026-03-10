package ftn.siit.nvt.dto.warehouse;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class WarehouseDTO {
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
    private List<SectorDTO> sectors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
