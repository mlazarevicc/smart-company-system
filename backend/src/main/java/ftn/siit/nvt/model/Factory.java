package ftn.siit.nvt.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "factories", indexes = {
        @Index(name = "idx_factory_name", columnList = "name"),
        @Index(name = "idx_factory_online", columnList = "is_online")
})
@Data
public class Factory {
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
    @CollectionTable(name = "factory_images", joinColumns = @JoinColumn(name = "factory_id"))
    @Column(name = "image_url")
    private Set<String> imageUrls = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "factories")
    private Set<Product> products = new HashSet<>();

    @Column(nullable = false)
    private Boolean isOnline = false;

    private LocalDateTime lastHeartbeat;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_manager_id")
    private Manager createdBy;
}