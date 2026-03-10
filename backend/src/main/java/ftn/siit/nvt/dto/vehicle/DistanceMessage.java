package ftn.siit.nvt.dto.vehicle;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class DistanceMessage {
    private Long vehicleId;
    private OffsetDateTime timestamp;
    private Double distance;
    private Double longitude;
    private Double latitude;
}
