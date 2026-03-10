package ftn.siit.nvt.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCompanyRequest {
    @NotBlank(message = "Reason is required")
    private String reason;

    @NotNull(message = "Approved is required")
    private Boolean approved;
}
