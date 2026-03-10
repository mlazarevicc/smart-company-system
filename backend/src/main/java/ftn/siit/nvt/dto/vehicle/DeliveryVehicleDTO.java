package ftn.siit.nvt.dto.vehicle;

import jakarta.persistence.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class DeliveryVehicleDTO implements Serializable {
    private Long id;
    private String registrationNumber;
    private Double weightLimit;
    private Long makeId;
    private Long modelId;
    private String makeName;
    private String modelName;
    private Set<String> images;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;
    private Double lastLatitude;
    private Double lastLongitude;
    private LocalDateTime lastLocationReadingAt;
}
