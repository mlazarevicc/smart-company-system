package ftn.siit.nvt.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSectorRequest {

    @NotBlank(message = "Sector name is required")
    private String name;
    private String description;
    private Long version;
}
