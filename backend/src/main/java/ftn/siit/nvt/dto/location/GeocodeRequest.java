package ftn.siit.nvt.dto.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeocodeRequest {

    @NotBlank(message = "Street address is required")
    private String streetAddress;

    @NotNull(message = "City ID is required")
    private Long cityId;
}
