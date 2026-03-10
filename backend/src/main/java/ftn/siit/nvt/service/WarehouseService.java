package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.warehouse.*;
import ftn.siit.nvt.model.*;
import ftn.siit.nvt.repository.*;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final SectorRepository sectorRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final FileStorageService fileStorageService;

    @CacheEvict(value = "warehouses_filtered", allEntries = true)
    public WarehouseDTO createWarehouse(CreateWarehouseRequest request, MultipartFile[] images, Manager creator) {
        List<String> uploadedImageUrls = new ArrayList<>();
        if (images != null && images.length > 0) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.saveWarehouseImage(image, request.getName());
                        uploadedImageUrls.add(imageUrl);
                    } catch (Exception e) {
                        log.warn("Failed to upload image: {}", e.getMessage());
                    }
                }
            }
        }
        return saveWarehouseToDb(request, uploadedImageUrls, creator);
    }

    @Transactional(rollbackFor = Exception.class)
    public WarehouseDTO saveWarehouseToDb(CreateWarehouseRequest request, List<String> imageUrls, Manager creator) {
        if (warehouseRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException(
                    "Warehouse with name already exists: " + request.getName()
            );
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getCountryId()));

        if (!city.getCountry().getId().equals(country.getId())) {
            throw new IllegalArgumentException(
                    "City " + city.getName() + " does not belong to country " + country.getName()
            );
        }

        Warehouse warehouse = new Warehouse();
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(city);
        warehouse.setCountry(country);
        warehouse.setLatitude(request.getLatitude());
        warehouse.setLongitude(request.getLongitude());
        warehouse.setCreatedBy(creator);
        warehouse.setIsOnline(false);

        // Default image if upload failed
        if (imageUrls.isEmpty()) {
            warehouse.getImageUrls().add(fileStorageService.getDefaultWarehouseImageUrl());
        } else {
            warehouse.getImageUrls().addAll(imageUrls);
        }

        // Create sectors
        for (CreateSectorRequest sectorReq : request.getSectors()) {
            Sector sector = new Sector();
            sector.setName(sectorReq.getName());
            sector.setDescription(sectorReq.getDescription());
            sector.setWarehouse(warehouse);
            warehouse.getSectors().add(sector);
        }

//        log.info("Creating warehouse: name={}, cityId={}, countryId={}, sectors={}",
//                warehouse.getName(), request.getCityId(), request.getCountryId(),
//                request.getSectors().size()
//        );

        Warehouse saved = warehouseRepository.save(warehouse);
        return convertToDTO(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "warehouse_by_id", key = "#id")
    public WarehouseDTO getWarehouseById(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return convertToDTO(warehouse);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "warehouses_filtered", key = "T(java.lang.String).valueOf(#search) + '-' + T(java.lang.String).valueOf(#country) + '-' + T(java.lang.String).valueOf(#status) + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public PaginatedResponse<WarehouseDTO> findWarehousesWithFilters(
            String search,
            String country,
            String status,
            Pageable pageable
    ) {
        Boolean isOnline = null;
        if (status != null && !status.isEmpty()) {
            isOnline = status.equalsIgnoreCase("online");
        }
        Page<Warehouse> warehouses;
        if (search != null && !search.isEmpty()) {
            warehouses = warehouseRepository.findBySearchQueryAndFilters(
                    search, country, isOnline, pageable
            );
        } else {
            warehouses = warehouseRepository.findByFilters(
                    country, isOnline, pageable
            );
        }
        Page<WarehouseDTO> dtosPage = warehouses.map(this::convertToDTO);

        return new PaginatedResponse<>(dtosPage);
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse_by_id", key = "#id"),
            @CacheEvict(value = "warehouses_filtered", allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public WarehouseDTO updateWarehouse(
            Long id,
            UpdateWarehouseRequest request,
            MultipartFile[] images
    ) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        if (request.getVersion() != null && !warehouse.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Warehouse was updated by another user. Please refresh and try again."
            );
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            if (!warehouse.getName().equals(request.getName()) &&
                    warehouseRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("Warehouse with name already exists: " + request.getName());
            }
            warehouse.setName(request.getName());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            warehouse.setAddress(request.getAddress());
        }

        if (request.getLatitude() != null) {
            warehouse.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            warehouse.setLongitude(request.getLongitude());
        }

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
            warehouse.setCity(city);
        }

        if (request.getCountryId() != null) {
            Country country = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getCountryId()));
            warehouse.setCountry(country);
        }

        if (!warehouse.getCity().getCountry().getId().equals(warehouse.getCountry().getId())) {
            throw new IllegalArgumentException(
                    "City " + warehouse.getCity().getName() +
                            " does not belong to country " + warehouse.getCountry().getName()
            );
        }
        if (images != null && images.length > 0) {

            for (String url : warehouse.getImageUrls()) {
                try {
                    fileStorageService.deleteByUrl(url);
                } catch (Exception e) {
                    log.warn("Failed to delete old image {}", url, e);
                }
            }

            warehouse.getImageUrls().clear();

            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.saveWarehouseImage(image, warehouse.getName());
                        warehouse.getImageUrls().add(imageUrl);
                    } catch (Exception e) {
                        log.error("Failed to upload image: {}", e.getMessage(), e);
                    }
                }
            }
            if (warehouse.getImageUrls().isEmpty()) {
                warehouse.getImageUrls().add(fileStorageService.getDefaultWarehouseImageUrl());
            }
        }

        Warehouse updated = warehouseRepository.save(warehouse);
        return convertToDTO(updated);
    }

    @Caching(evict = {
            @CacheEvict(value = "warehouse_by_id", key = "#id"),
            @CacheEvict(value = "warehouses_filtered", allEntries = true)
    })
    public void deleteWarehouse(Long id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));

        // Delete all images
        for (String imageUrl : warehouse.getImageUrls()) {
            try {
                fileStorageService.deleteByUrl(imageUrl);
            } catch (Exception e) {
                log.warn("Failed to delete image: {}", e.getMessage());
            }
        }

        warehouseRepository.delete(warehouse);
        log.info("Deleted warehouse: id={}, name={}", id, warehouse.getName());
    }

    public void recordHeartbeat(Long warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        warehouse.setIsOnline(true);
        warehouse.setLastHeartbeat(LocalDateTime.now());
        warehouseRepository.save(warehouse);

    }

    public List<SimulatorWarehouseConfigDTO> getWarehousesForSimulator(Long startId, Long endId) {
        List<Warehouse> warehouses = warehouseRepository.findByIdBetween(startId, endId);

        return warehouses.stream().map(warehouse -> {
            List<SimulatorWarehouseConfigDTO.SimulatorSectorConfigDTO> sectorDTOs = warehouse.getSectors().stream()
                    .map(sector -> new SimulatorWarehouseConfigDTO.SimulatorSectorConfigDTO(
                            sector.getId(),
                            sector.getName()
                    ))
                    .collect(Collectors.toList());

            return new SimulatorWarehouseConfigDTO(
                    warehouse.getId(),
                    warehouse.getName(),
                    sectorDTOs
            );
        }).collect(Collectors.toList());
    }


    // ===== SECTOR MANAGEMENT =====

    public SectorDTO getSector(Long warehouseId, Long sectorId) {
        Sector sector = sectorRepository.findByIdAndWarehouseId(sectorId, warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));
        return convertSectorToDTO(sector);
    }

    public SectorDTO addSector(Long warehouseId, CreateSectorRequest request) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        Sector sector = new Sector();
        sector.setName(request.getName());
        sector.setDescription(request.getDescription());
        sector.setWarehouse(warehouse);

        Sector saved = sectorRepository.save(sector);
//        log.info("Added sector '{}' to warehouse '{}'", saved.getName(), warehouse.getName());

        return convertSectorToDTO(saved);
    }

    @Transactional(rollbackFor = Exception.class)
    public SectorDTO updateSector(Long warehouseId, Long sectorId, CreateSectorRequest request) {
        warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));

        if (!sector.getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException(
                    "Sector " + sectorId + " does not belong to warehouse " + warehouseId
            );
        }

        if (request.getVersion() != null && !sector.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException(
                    "Sector was updated by another user. Please refresh and try again."
            );
        }

        sector.setName(request.getName());
        sector.setDescription(request.getDescription());

        Sector updated = sectorRepository.save(sector);
        return convertSectorToDTO(updated);
    }


    @Transactional
    public void updateSectorTemperature(Long warehouseId, Long sectorId, Double temperature) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));

        if (!sector.getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException("Sector does not belong to warehouse");
        }

        sector.setLastTemperature(temperature);
        sector.setLastTemperatureReadingAt(LocalDateTime.now());
        sectorRepository.save(sector);

        log.info("Updated sector {} temperature to {}°C", sectorId, temperature);
    }


    public void deleteSector(Long warehouseId, Long sectorId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", warehouseId));

        Sector sector = sectorRepository.findById(sectorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sector", "id", sectorId));

        // Validate sector belongs to warehouse
        if (!sector.getWarehouse().getId().equals(warehouseId)) {
            throw new IllegalArgumentException(
                    "Sector " + sectorId + " does not belong to warehouse " + warehouseId
            );
        }

        sectorRepository.delete(sector);
        log.info("Deleted sector: id={}, name={}", sectorId, sector.getName());
    }

    // ===== DTO CONVERSION =====

    private WarehouseDTO convertToDTO(Warehouse warehouse) {
        WarehouseDTO dto = new WarehouseDTO();
        dto.setId(warehouse.getId());
        dto.setName(warehouse.getName());
        dto.setAddress(warehouse.getAddress());
        dto.setCity(warehouse.getCity().getName());
        dto.setCountry(warehouse.getCountry().getName());
        dto.setLatitude(warehouse.getLatitude());
        dto.setLongitude(warehouse.getLongitude());
        dto.setIsOnline(warehouse.getIsOnline());
        dto.setLastHeartbeat(warehouse.getLastHeartbeat());
        dto.setCreatedAt(warehouse.getCreatedAt());
        dto.setUpdatedAt(warehouse.getUpdatedAt());
        dto.setVersion(warehouse.getVersion());

        if (warehouse.getImageUrls() != null) {
            dto.setImageUrls(new HashSet<>(warehouse.getImageUrls()));
        }

        if (warehouse.getSectors() != null) {
            List<SectorDTO> sectorDTOs = warehouse.getSectors()
                    .stream()
                    .map(this::convertSectorToDTO)
                    .collect(Collectors.toList());

            dto.setSectors(new ArrayList<>(sectorDTOs));
        }

        return dto;
    }

    private SectorDTO convertSectorToDTO(Sector sector) {
        SectorDTO dto = new SectorDTO();
        dto.setId(sector.getId());
        dto.setName(sector.getName());
        dto.setDescription(sector.getDescription());
        dto.setLastTemperature(sector.getLastTemperature());
        dto.setLastTemperatureReadingAt(sector.getLastTemperatureReadingAt());
        dto.setCreatedAt(sector.getCreatedAt());
        dto.setUpdatedAt(sector.getUpdatedAt());
        dto.setVersion(sector.getVersion());
        dto.setWarehouseId(sector.getWarehouse().getId());
        dto.setWarehouseName(sector.getWarehouse().getName());
        return dto;
    }
}
