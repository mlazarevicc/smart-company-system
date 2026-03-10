package ftn.siit.nvt.dto.location;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReverseGeocodeRequest {
    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}

