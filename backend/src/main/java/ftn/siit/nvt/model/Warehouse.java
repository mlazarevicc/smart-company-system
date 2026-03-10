package ftn.siit.nvt.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "warehouses", indexes = {
        @Index(name = "idx_warehouse_name", columnList = "name"),
        @Index(name = "idx_warehouse_online", columnList = "is_online")
})
@Data
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    @CollectionTable(name = "warehouse_images", joinColumns = @JoinColumn(name = "warehouse_id"))
    @Column(name = "image_url")
    private Set<String> imageUrls = new HashSet<>();

    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sector> sectors = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isOnline = false;

    private LocalDateTime lastHeartbeat;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by_manager_id")
    private Manager createdBy;
}
