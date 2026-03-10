package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.factory.*;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.enums.TimePeriod;
import ftn.siit.nvt.repository.FactoryRepository;
import ftn.siit.nvt.service.FactoryService;
import ftn.siit.nvt.service.FactoryAnalyticsService;
import ftn.siit.nvt.utils.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/factories")
@Validated
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class FactoryController {

    private final FactoryService factoryService;
    private final FactoryAnalyticsService analyticsService;
    private final FactoryRepository factoryRepository;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FactoryDTO> createFactory(
            @RequestPart("data") @Valid CreateFactoryRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @AuthenticationPrincipal Manager currentManager
    ) {
        FactoryDTO created = factoryService.createFactory(request, images, currentManager);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<Page<FactoryDTO>> getAllFactories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(sort).descending());
        Page<FactoryDTO> factories = factoryService.getAllFactories(pageable);
        return ResponseEntity.ok(factories);
    }

    @GetMapping("/simple")
    public ResponseEntity<List<FactorySimpleDTO>> getAllFactoriesSimple() {
        List<FactorySimpleDTO> factories = factoryRepository.findAll()
                .stream()
                .map(factory -> new FactorySimpleDTO(factory.getId(), factory.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(factories);
    }

    // GET /api/factories/filter?query=Belgrade&countryId=1&online=true&page=0&size=20&sort=name,asc
    @GetMapping("/filter")
    public ResponseEntity<PaginatedResponse<FactoryDTO>> filterFactories(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean online,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDir = sortParams.length > 1 ? sortParams[1] : "desc";

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String actualSortProperty = switch (sortField) {
            case "name" -> "name";
            case "city" -> "city_id";
            case "isOnline" -> "is_online";
            case "createdAt" -> "created_at";
            default -> "created_at";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, actualSortProperty));

        FactoryFilterRequest filter = new FactoryFilterRequest(query, countryId, cityId, online);

        return ResponseEntity.ok(factoryService.filterFactories(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FactoryDTO> getFactoryById(@PathVariable Long id) {
        FactoryDTO factory = factoryService.getFactoryById(id);
        return ResponseEntity.ok(factory);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPERMANAGER')")
    public ResponseEntity<FactoryDTO> updateFactory(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateFactoryRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images
    ) {
        FactoryDTO updated = factoryService.updateFactory(id, request, images);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFactory(@PathVariable Long id) {
        factoryService.deleteFactory(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<Void> recordHeartbeat(@PathVariable Long id) {
        factoryService.recordHeartbeat(id);
        return ResponseEntity.ok().build();
    }

    // ===== ANALYTICS =====

    @GetMapping("/{id}/production-analytics")
    public ResponseEntity<ProductionAnalyticsDTO> getProductionAnalytics(
            @PathVariable Long id,
            @RequestParam Long productId,
            @RequestParam TimePeriod period,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
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
        } else {
            startDate = period.getStartDate();
        }

        String granularity = determineGranularity(startDate, endDate);
        ProductionAnalyticsDTO analytics = analyticsService.getProductionAnalytics(
                id, productId, startDate, endDate, granularity
        );
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/{id}/availability-analytics")
    public ResponseEntity<AvailabilityAnalyticsDTO> getAvailabilityAnalytics(
            @PathVariable Long id,
            @RequestParam TimePeriod period,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
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

        AvailabilityAnalyticsDTO analytics = analyticsService.getAvailabilityAnalytics(
                id, startDate, endDate, granularity
        );

        return ResponseEntity.ok(analytics);
    }


    @GetMapping("/{id}/metrics/current")
    @PreAuthorize("hasAnyRole('MANAGER', 'SUPERMANAGER')")
    public ResponseEntity<FactoryMetricsDTO> getCurrentMetrics(@PathVariable Long id) {
        FactoryMetricsDTO metrics = analyticsService.getCurrentMetrics(id);
        return ResponseEntity.ok(metrics);
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


    // ===== SIMULATOR =====
    @GetMapping("/simulator-config")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SimulatorFactoryConfigDTO>> getSimulatorConfig(
            @RequestParam(defaultValue = "1") Long startId,
            @RequestParam(defaultValue = "1000") Long endId) {
        List<SimulatorFactoryConfigDTO> configs = factoryService.getFactoriesForSimulator(startId, endId);
        return ResponseEntity.ok(configs);
    }
}
