package ftn.siit.nvt.dto.factory;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class HeartbeatMessage {
    private Long factoryId;
    private OffsetDateTime timestamp;
    private String status;
}
