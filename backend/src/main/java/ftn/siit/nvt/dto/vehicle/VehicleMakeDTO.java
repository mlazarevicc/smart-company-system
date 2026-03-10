package ftn.siit.nvt.dto.vehicle;

import ftn.siit.nvt.model.VehicleMake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleMakeDTO implements Serializable {
    private Long id;
    private String name;

    public VehicleMakeDTO(VehicleMake make) {
        this.id = make.getId();
        this.name = make.getName();
    }
}
