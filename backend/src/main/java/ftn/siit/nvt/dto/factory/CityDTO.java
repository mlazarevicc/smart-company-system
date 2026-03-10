package ftn.siit.nvt.dto.factory;

import lombok.Data;

import java.io.Serializable;

@Data
public class CityDTO implements Serializable {
    private Long id;
    private String name;
    private Long countryId;
    private String countryName;
    private Double latitude;
    private Double longitude;
}
