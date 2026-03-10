package ftn.siit.nvt.dto.vehicle;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
public class HeartbeatMessage {
    private Long vehicleId;
    private OffsetDateTime timestamp;
    private String status;
}
