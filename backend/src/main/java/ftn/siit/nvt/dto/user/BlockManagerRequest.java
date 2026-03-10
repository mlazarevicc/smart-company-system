package ftn.siit.nvt.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockManagerRequest {

//    @NotBlank(message = "Reason is required")
    private String reason;
    private String details;
}