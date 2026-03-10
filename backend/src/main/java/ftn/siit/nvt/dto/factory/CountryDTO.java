package ftn.siit.nvt.dto.factory;

import lombok.Data;

import java.io.Serializable;

@Data
public class CountryDTO implements Serializable {
    private Long id;
    private String name;
    private String code;
}
