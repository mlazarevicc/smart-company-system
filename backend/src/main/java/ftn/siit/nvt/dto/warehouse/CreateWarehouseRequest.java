package ftn.siit.nvt.dto.warehouse;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class CreateWarehouseRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "City ID is required")
    private Long cityId;

    @NotNull(message = "Country ID is required")
    private Long countryId;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    @NotEmpty(message = "At least one sector must be defined")
    private List<CreateSectorRequest> sectors;
}
