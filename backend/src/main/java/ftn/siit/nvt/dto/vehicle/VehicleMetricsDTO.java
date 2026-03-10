package ftn.siit.nvt.dto.vehicle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleMetricsDTO {
    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private Boolean isOnline;
    private LocalDateTime lastHeartbeat;

    private Double lastLongitude;
    private Double lastLatitude;
    private LocalDateTime lastLocationReadingTime;

    private Double lastRecordedDistance;
    private LocalDateTime lastInfluxReadingTime;
}
