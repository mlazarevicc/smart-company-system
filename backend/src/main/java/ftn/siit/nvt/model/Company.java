package ftn.siit.nvt.model;

import ftn.siit.nvt.model.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @ManyToOne
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;

    private Double latitude;
    private Double longitude;

    @ElementCollection
    @CollectionTable(name = "company_images", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "image_url")
    private Set<String> images = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "company_proof", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "proof_url")
    private Set<String> proofOfOwnership = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer owner;
}
