package ftn.siit.nvt.dto.factory;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.Set;

@Data
public class CreateFactoryRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "City ID is required")
    private Long cityId;

    @NotNull(message = "Country ID is required")
    private Long countryId;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

//    @NotEmpty(message = "At least one product must be selected")
    private Set<Long> productIds;
}
