package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.warehouse.TemperatureMessage;
import ftn.siit.nvt.service.WarehouseTemperatureService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/temperature")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TemperatureController {

    private final WarehouseTemperatureService temperatureService;

    @PostMapping
    public ResponseEntity<Void> recordTemperature(@RequestBody TemperatureRequest request) {
        temperatureService.recordTemperatureReading(
                request.getWarehouseId(),
                request.getSectorId(),
                request.getTemperature()
        );
        return ResponseEntity.ok().build();
    }

    @Data
    public static class TemperatureRequest {
        private Long warehouseId;
        private Long sectorId;
        private Double temperature;
        private LocalDateTime timestamp;
    }
}