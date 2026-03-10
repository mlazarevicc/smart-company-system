package ftn.siit.nvt.dto.factory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FactorySimpleDTO implements Serializable {
    private Long id;
    private String name;
}

