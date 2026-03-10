package ftn.siit.nvt.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "vehicle_makes")
@Data
public class VehicleMake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;
}
