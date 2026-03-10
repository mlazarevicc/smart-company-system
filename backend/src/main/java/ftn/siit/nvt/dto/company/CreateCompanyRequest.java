package ftn.siit.nvt.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCompanyRequest {
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
}
