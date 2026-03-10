package ftn.siit.nvt.dto.company;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class CompanyDTO implements Serializable {
    private Long id;
    private Boolean status;
    private String name;
    private String address;
    private String city;
    private String country;
    private Double latitude;
    private Double longitude;
    private Set<String> images;
    private Set<String> proofOfOwnershipUrls;
    private String ownerName;
}
