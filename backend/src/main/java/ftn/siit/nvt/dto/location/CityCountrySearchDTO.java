package ftn.siit.nvt.dto.location;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CityCountrySearchDTO {
    private Long cityId;
    private String cityName;
    private Long countryId;
    private String countryName;
    private String countryCode;
    private Double cityLatitude;
    private Double cityLongitude;
}
