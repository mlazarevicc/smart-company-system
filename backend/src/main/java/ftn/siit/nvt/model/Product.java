package ftn.siit.nvt.model;

import ftn.siit.nvt.model.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products")
@SQLRestriction("is_deleted = false")
@Getter
@Setter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ProductCategory category;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_manager_id", nullable = false)
    private Manager createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "product_factory",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "factory_id")
    )
    private Set<Factory> factories = new HashSet<>();
}