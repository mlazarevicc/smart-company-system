package ftn.siit.nvt.dto.vehicle;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVehicleRequest {
    @NotNull(message = "Version is required (for optimistic locking)")
    private Long version;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Weight limit is required")
    @Min(value = 0, message = "Weight limit must be over 0")
    private Double weightLimit;

    @NotNull(message = "Make ID is required")
    private Long makeId;

    @NotNull(message = "Model ID is required")
    private Long modelId;
}
