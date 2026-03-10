package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.vehicle.*;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.User;
import ftn.siit.nvt.model.enums.TimePeriod;
import ftn.siit.nvt.repository.ManagerRepository;
import ftn.siit.nvt.service.DeliveryVehicleAnalyticsService;
import ftn.siit.nvt.service.DeliveryVehicleService;
import ftn.siit.nvt.service.VehicleMakeService;
import ftn.siit.nvt.service.VehicleModelService;
import ftn.siit.nvt.utils.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@Validated
@Slf4j
public class DeliveryVehicleController {

    private final DeliveryVehicleService vehicleService;
    private final ManagerRepository managerRepository;
    private final DeliveryVehicleAnalyticsService analyticsService;
    private final VehicleMakeService vehicleMakeService;
    private final VehicleModelService vehicleModelService;

    public DeliveryVehicleController(DeliveryVehicleService vehicleService, ManagerRepository managerRepository, DeliveryVehicleAnalyticsService analyticsService, VehicleMakeService vehicleMakeService, VehicleModelService vehicleModelService) {
        this.vehicleService = vehicleService;
        this.managerRepository = managerRepository;
        this.analyticsService = analyticsService;
        this.vehicleMakeService = vehicleMakeService;
        this.vehicleModelService = vehicleModelService;
    }

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DeliveryVehicleDTO> createVehicle(
            @RequestPart("data") @Valid CreateVehicleRequest request,
            @RequestPart(value = "image", required = false) MultipartFile[] images,
            @AuthenticationPrincipal User currentUser
    ) {
        Manager manager = managerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        DeliveryVehicleDTO vehicle = vehicleService.createVehicle(request, images, manager);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicle);
    }

    /* ===================== READ ===================== */

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryVehicleDTO> getVehicleById(@PathVariable Long id) {
        return ResponseEntity.ok(vehicleService.getVehicleById(id));
    }

    @GetMapping("/makes")
    public ResponseEntity<List<VehicleMakeDTO>> getAllMakes() {
        return ResponseEntity.ok(vehicleMakeService.getAllMakes());
    }

    @GetMapping("/make/{makeId}/models")
    public ResponseEntity<List<VehicleModelDTO>> getModelsForMake(@PathVariable Long makeId) {
        return ResponseEntity.ok(vehicleModelService.getAllModelsByMakeId(makeId));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<DeliveryVehicleDTO>> getVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long makeId,
            @RequestParam(required = false) Long modelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        size = Math.min(size, 100);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

        // If no filters, use getAll; otherwise use search
        if (search == null && makeId == null && modelId == null) {
            return ResponseEntity.ok(vehicleService.getAllVehicles(pageable));
        } else {
            return ResponseEntity.ok(
                    vehicleService.searchVehicles(search, makeId, modelId, pageable)
            );
        }
    }

    /* ===================== UPDATE ===================== */

    @PutMapping(value = "/{id}", consumes =  MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<DeliveryVehicleDTO> updateVehicle(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateVehicleRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        return ResponseEntity.ok(
                vehicleService.updateVehicle(id, request, images)
        );
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/simulator-config")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SimulatorVehicleConfigDTO>> getSimulatorConfig(
            @RequestParam(defaultValue = "1") Long startId,
            @RequestParam(defaultValue = "1000") Long endId) {
        List<SimulatorVehicleConfigDTO> configs = vehicleService.getVehiclesForSimulator(startId, endId);
        return ResponseEntity.ok(configs);
    }

    // METRICS
    @GetMapping("/{vehicleId}/metrics/current")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VehicleMetricsDTO> getCurrentMetrics(@PathVariable Long vehicleId) {
        VehicleMetricsDTO metrics = analyticsService.getCurrentMetrics(vehicleId);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{vehicleId}/analytics/availability")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VehicleAvailabilityMetricsDTO> getAvailabilityAnalytics(
            @PathVariable Long vehicleId,
            @RequestParam TimePeriod period,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {
        LocalDateTime startDate;
        LocalDateTime endDate = LocalDateTime.now();

        if (period == TimePeriod.CUSTOM) {
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException(
                        "CUSTOM period requires both fromDate and toDate parameters"
                );
            }

            startDate = fromDate.atStartOfDay();
            endDate = toDate.atTime(23, 59, 59);

            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween > 365) {
                throw new IllegalArgumentException(
                        "Date range cannot exceed 1 year (365 days). Current range: " + daysBetween + " days"
                );
            }
        } else {
            startDate = period.getStartDate();
        }

        String granularity = determineGranularity(startDate, endDate);

        VehicleAvailabilityMetricsDTO metrics = analyticsService.getAvailabilityAnalytics(
                vehicleId, startDate, endDate, granularity);

        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/{vehicleId}/analytics/distance")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<VehicleDistanceMetricsDTO> getDistanceAnalytics(
            @PathVariable Long vehicleId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "1h") String granularity) {
        VehicleDistanceMetricsDTO analytics = analyticsService.getDistanceMetrics(
                vehicleId, startDate, endDate, granularity
        );
        return ResponseEntity.ok(analytics);
    }

    private String determineGranularity(LocalDateTime start, LocalDateTime end) {
        long hoursBetween = ChronoUnit.HOURS.between(start, end);
        long daysBetween = ChronoUnit.DAYS.between(start, end);

        if (hoursBetween <= 3) {
            return "1m";   // Real-time view: 1-minute granularity
        } else if (hoursBetween <= 12) {
            return "1h";   // 12-hour view: hourly granularity
        } else if (hoursBetween <= 48) {
            return "1h";   // Up to 2 days: hourly
        } else if (daysBetween <= 7) {
            return "1h";   // 1 week: hourly
        } else if (daysBetween <= 30) {
            return "1d";   // 1 month: daily
        } else if (daysBetween <= 90) {
            return "1w";   // 3 months: weekly
        } else {
            return "1mo";  // 1 year: monthly
        }
    }
}
