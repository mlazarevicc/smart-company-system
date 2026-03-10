package ftn.siit.nvt.dto.warehouse;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HeartbeatMessage {
    private Long warehouseId;
    private LocalDateTime timestamp;
    private String status;
}
