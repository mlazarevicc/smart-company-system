package ftn.siit.nvt.dto.factory;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFactoryRequest {
    private String name;
    private String address;
    private Long cityId;
    private Long countryId;
    private Double latitude;
    private Double longitude;
    private Set<Long> productIds;
    @NotNull(message = "Version is required (for optimistic locking)")
    private Long version;
}
