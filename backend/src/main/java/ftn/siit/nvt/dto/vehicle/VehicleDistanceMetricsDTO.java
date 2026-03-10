package ftn.siit.nvt.dto.vehicle;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class VehicleDistanceMetricsDTO {

    private Long vehicleId;
    private String vehicleRegistrationNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String granularity;

    private List<DistanceDataPoint> dataPoints = new ArrayList<>();

    @Getter
    @Setter
    public static class DistanceDataPoint {
        private LocalDateTime timestamp;
        private Double distance;
    }
}
