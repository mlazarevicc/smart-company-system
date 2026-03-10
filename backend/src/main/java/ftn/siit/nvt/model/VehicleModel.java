package ftn.siit.nvt.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_models")
@Data
public class VehicleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "make_id", nullable = false)
    private VehicleMake make;
}
