package ftn.siit.nvt.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "countries")
@Data
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true, length = 2)
    private String code;
}
