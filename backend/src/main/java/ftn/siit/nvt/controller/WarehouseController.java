package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.warehouse.*;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.service.WarehouseService;
import ftn.siit.nvt.service.WarehouseAnalyticsService;
import ftn.siit.nvt.utils.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/warehouses")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final WarehouseAnalyticsService analyticsService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WarehouseDTO> createWarehouse(
            @RequestPart("data") @Valid CreateWarehouseRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal Manager currentManager
    ) {
        WarehouseDTO created = warehouseService.createWarehouse(request, images, currentManager);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<WarehouseDTO>> findWarehousesWithFilters(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        PaginatedResponse<WarehouseDTO> response = warehouseService.findWarehousesWithFilters(search, country, status, pageable);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDTO> getWarehouseById(@PathVariable Long id) {
        WarehouseDTO warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WarehouseDTO> updateWarehouse(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateWarehouseRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        WarehouseDTO updated = warehouseService.updateWarehouse(id, request, images);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> recordHeartbeat(@PathVariable Long id) {
        warehouseService.recordHeartbeat(id);
        return ResponseEntity.ok().build();
    }

    // ===== SECTOR MANAGEMENT =====

    @GetMapping("/{warehouseId}/sectors/{sectorId}")
    public ResponseEntity<SectorDTO> getSector(
            @PathVariable Long warehouseId,
            @PathVariable Long sectorId
    ) {
        SectorDTO sector = warehouseService.getSector(warehouseId, sectorId);
        return ResponseEntity.ok(sector);
    }

    @PostMapping("/{warehouseId}/sectors")
    public ResponseEntity<SectorDTO> addSector(
            @PathVariable Long warehouseId,
            @RequestBody @Valid CreateSectorRequest request
    ) {
        SectorDTO sector = warehouseService.addSector(warehouseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(sector);
    }

    @PutMapping("/{warehouseId}/sectors/{sectorId}")
    public ResponseEntity<SectorDTO> updateSector(
            @PathVariable Long warehouseId,
            @PathVariable Long sectorId,
            @RequestBody @Valid CreateSectorRequest request
    ) {
        SectorDTO updated = warehouseService.updateSector(warehouseId, sectorId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{warehouseId}/sectors/{sectorId}")
    public ResponseEntity<Void> deleteSector(
            @PathVariable Long warehouseId,
            @PathVariable Long sectorId
    ) {
        warehouseService.deleteSector(warehouseId, sectorId);
        return ResponseEntity.noContent().build();
    }

    // ===== ANALYTICS =====

    /**
     * GET /api/warehouses/{id}/analytics/temperature
     * Get temperature analytics for a specific sector in a warehouse.
     */
    @GetMapping("/{warehouseId}/analytics/temperature")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<TemperatureAnalyticsDTO> getTemperatureAnalytics(
            @PathVariable Long warehouseId,
            @RequestParam Long sectorId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "15m") String granularity) {
        TemperatureAnalyticsDTO analytics = analyticsService.getTemperatureAnalytics(
                warehouseId, sectorId, startDate, endDate, granularity
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{warehouseId}/analytics/availability")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<WarehouseAvailabilityAnalyticsDTO> getAvailabilityAnalytics(
            @PathVariable Long warehouseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1h") String granularity) {
        WarehouseAvailabilityAnalyticsDTO analytics = analyticsService.getAvailabilityAnalytics(
                warehouseId, startDate, endDate, granularity
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{warehouseId}/metrics/current")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<WarehouseMetricsDTO> getCurrentMetrics(@PathVariable Long warehouseId) {
        WarehouseMetricsDTO metrics = analyticsService.getCurrentMetrics(warehouseId);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/simulator-config")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SimulatorWarehouseConfigDTO>> getSimulatorConfig(
            @RequestParam(defaultValue = "1") Long startId,
            @RequestParam(defaultValue = "1000") Long endId) {
        List<SimulatorWarehouseConfigDTO> configs = warehouseService.getWarehousesForSimulator(startId, endId);
        return ResponseEntity.ok(configs);
    }


}