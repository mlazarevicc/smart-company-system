package ftn.siit.nvt.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GeocodeResponse {
    private Double latitude;
    private Double longitude;
    private String formattedAddress;
}
