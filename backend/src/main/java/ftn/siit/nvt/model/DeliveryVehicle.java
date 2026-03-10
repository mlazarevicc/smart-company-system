package ftn.siit.nvt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
@Data
public class DeliveryVehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    @Column(nullable = false)
    private Double weightLimit;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "make_id", nullable = false)
    private VehicleMake make;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vehicle_images", joinColumns = @JoinColumn(name = "vehicle_id"))
    @Column(name = "image_url")
    private Set<String> images = new HashSet<>();

    @Version
    @Column(name = "version")
    private Long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_manager_id")
    @JsonIgnore
    private Manager createdBy;

    @Column(nullable = false)
    private Boolean isOnline = false;

    private LocalDateTime lastHeartbeat;

    @Column(nullable = true)
    private Double lastLatitude;

    @Column(nullable = true)
    private Double lastLongitude;

    private LocalDateTime lastLocationReadingAt;
}
