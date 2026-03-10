package ftn.siit.nvt.dto.vehicle;

import ftn.siit.nvt.model.VehicleModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDTO implements Serializable {
    private Long id;
    private String name;

    public VehicleModelDTO(VehicleModel model) {
        this.id = model.getId();
        this.name = model.getName();
    }
}
