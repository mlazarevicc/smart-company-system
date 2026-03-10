package ftn.siit.nvt.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReverseGeocodeResponse {
    private String streetAddress;
    private String cityName;
    private String countryName;
    private String countryCode;
    private Long cityId;
    private Long countryId;
}

