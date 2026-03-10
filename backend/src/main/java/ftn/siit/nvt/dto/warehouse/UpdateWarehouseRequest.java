package ftn.siit.nvt.dto.warehouse;

import lombok.Data;

@Data
public class UpdateWarehouseRequest {
    private String name;
    private String address;
    private Long cityId;
    private Long countryId;
    private Double latitude;
    private Double longitude;
    private Long version;
}
