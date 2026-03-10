package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.product.ProductDTO;
import ftn.siit.nvt.dto.product.UpdateProductRequest;
import ftn.siit.nvt.dto.vehicle.CreateVehicleRequest;
import ftn.siit.nvt.dto.vehicle.DeliveryVehicleDTO;
import ftn.siit.nvt.dto.vehicle.SimulatorVehicleConfigDTO;
import ftn.siit.nvt.dto.vehicle.UpdateVehicleRequest;
import ftn.siit.nvt.exception.ConcurrentModificationException;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.*;
import ftn.siit.nvt.model.enums.ProductCategory;
import ftn.siit.nvt.repository.DeliveryVehicleRepository;
import ftn.siit.nvt.repository.VehicleMakeRepository;
import ftn.siit.nvt.repository.VehicleModelRepository;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeliveryVehicleService {

    private final DeliveryVehicleRepository vehicleRepository;
    private final VehicleMakeRepository vehicleMakeRepository;
    private final VehicleModelRepository vehicleModelRepository;
    private final FileStorageService fileStorageService;

    public DeliveryVehicleService(DeliveryVehicleRepository vehicleRepository, VehicleMakeRepository vehicleMakeRepository, VehicleModelRepository vehicleModelRepository, FileStorageService fileStorageService) {
        this.vehicleRepository = vehicleRepository;
        this.vehicleMakeRepository = vehicleMakeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
        this.fileStorageService = fileStorageService;
    }

    @CacheEvict(value = {"vehicles_page", "vehicles_search"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public DeliveryVehicleDTO createVehicle(CreateVehicleRequest request,
                                            MultipartFile[] images,
                                            Manager creator) {
        if (vehicleRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new IllegalArgumentException(
                    "Vehicle with registration number already exists: " + request.getRegistrationNumber()
            );
        }

        VehicleMake make = vehicleMakeRepository.findById(request.getMakeId())
                .orElseThrow(() -> new ResourceNotFoundException("Make", "id", request.getMakeId()));
        VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getModelId()));

        if (!model.getMake().getId().equals(make.getId())) {
            throw new IllegalArgumentException(
                    "Model " + model.getName() + " does not belong to make " + make.getName()
            );
        }

        DeliveryVehicle vehicle = new DeliveryVehicle();
        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setWeightLimit(request.getWeightLimit());
        vehicle.setCreatedBy(creator);


        if (images != null && images.length > 0) {
            log.warn("Got images: {}", images.length);
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.saveVehicleImage(
                                image,
                                vehicle.getRegistrationNumber()
                        );
                        vehicle.getImages().add(imageUrl);
                    } catch (Exception e) {
                        log.warn("Failed to upload image: {}", e.getMessage());
                    }
                }
            }
        }

        // Default image if upload failed
        if (vehicle.getImages().isEmpty()) {
            vehicle.getImages().add(fileStorageService.getDefaultVehicleImageUrl());
        }

        log.info("Creating vehicle: name={}, makeId={}, modelId={}",
                vehicle.getRegistrationNumber(), request.getMakeId(), request.getModelId()
        );

        DeliveryVehicle saved = vehicleRepository.save(vehicle);
        return convertToDTO(saved);
    }

    @Cacheable(value = "vehicle", key="#id")
    @Transactional(readOnly = true)
    public DeliveryVehicleDTO getVehicleById(Long id) {
        DeliveryVehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DeliveryVehicle", "id", id));
        return convertToDTO(vehicle);
    }


    @Cacheable(value = "vehicles_page", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @Transactional(readOnly = true)
    public PaginatedResponse<DeliveryVehicleDTO> getAllVehicles(Pageable pageable) {
        return new PaginatedResponse<>(vehicleRepository.findAll(pageable)
                .map(this::convertToDTO));
    }

    @Cacheable(
            value = "vehicles_search",
            key = "#query + '-' + #makeId + '-' + #modelId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize"
    )
    @Transactional(readOnly = true)
    public PaginatedResponse<DeliveryVehicleDTO> searchVehicles(
            String query,
            Long makeId,
            Long modelId,
            Pageable pageable
    ) {

        String q = (query == null || query.isBlank()) ? null : query;

        Page<DeliveryVehicle> result;

        if (q != null) {
            // Use ranked native search
            result = vehicleRepository.searchWithRank(
                    q,
                    makeId,
                    modelId,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize())
            );
        } else {
            // Use JPQL search with dynamic sorting
            result = vehicleRepository.searchWithoutRank(
                    makeId,
                    modelId,
                    pageable
            );
        }

        return new PaginatedResponse<>(result.map(this::convertToDTO));
    }

    @CacheEvict(value = {"vehicles_page", "vehicles_search"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public DeliveryVehicleDTO updateVehicle(Long id, UpdateVehicleRequest request, MultipartFile[] images) {
        DeliveryVehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        if (request.getVersion() == null) {
            throw new IllegalArgumentException("Version is required for update");
        }

        if (!vehicle.getVersion().equals(request.getVersion())) {
            throw new ConcurrentModificationException(
                    "Vehicle",
                    id,
                    request.getVersion(),
                    vehicle.getVersion()
            );
        }

        if (!vehicle.getRegistrationNumber().equals(request.getRegistrationNumber()) &&
                vehicleRepository.findByRegistrationNumber(request.getRegistrationNumber()).isPresent()) {
            throw new IllegalArgumentException("Vehicle with registration number already exists: " + request.getRegistrationNumber());
        }

        VehicleMake make = vehicleMakeRepository.findById(request.getMakeId())
                .orElseThrow(() -> new ResourceNotFoundException("Make", "id", request.getMakeId()));
        VehicleModel model = vehicleModelRepository.findById(request.getModelId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getModelId()));

        if (!model.getMake().getId().equals(make.getId())) {
            throw new IllegalArgumentException(
                    "Model " + model.getName() + " does not belong to make " + make.getName()
            );
        }

        if (images != null && images.length > 0) {
            for (String url : vehicle.getImages()) {
                try {
                    fileStorageService.deleteByUrl(url);
                }
                catch (Exception e) {
                    log.warn("Failed to delete old image {}", url, e);
                }
            }
        }

        vehicle.getImages().clear();

        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                try {
                    String imageUrl = fileStorageService.saveVehicleImage(image, vehicle.getRegistrationNumber());
                    vehicle.getImages().add(imageUrl);
                } catch (Exception e) {
                    log.error("Failed to upload image: {}", e.getMessage(), e);
                }
            }
        }
        if (vehicle.getImages().isEmpty()) {
            vehicle.getImages().add(fileStorageService.getDefaultVehicleImageUrl());
        }

        vehicle.setRegistrationNumber(request.getRegistrationNumber());
        vehicle.setWeightLimit(request.getWeightLimit());
        vehicle.setMake(make);
        vehicle.setModel(model);

        vehicle.setUpdatedAt(LocalDateTime.now());

        DeliveryVehicle saved = vehicleRepository.save(vehicle);
        return convertToDTO(saved);
    }

    @CacheEvict(value = {"vehicles_page", "vehicles_search"}, allEntries = true)
    @Transactional(rollbackFor = Exception.class)
    public void deleteVehicle(Long id) {
        DeliveryVehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        // Optional: Check if product is referenced in orders
        // if (hasActiveReferences(product.getId())) {
        //     throw new IllegalStateException("Cannot delete product with active references");
        // }

        Set<String> images = new HashSet<>(vehicle.getImages());

        // 1. Delete from DB FIRST
        vehicleRepository.delete(vehicle);
        vehicleRepository.flush(); // Ensure DB deletion

        // 2. Delete image file ONLY after successful DB deletion
        for (String imageUrl : images) {
            if (!imageUrl.equals(fileStorageService.getDefaultVehicleImageUrl())) {
                try {
                    fileStorageService.deleteByUrl(imageUrl);
                } catch (Exception e) {
                    // Log error but don't fail - orphaned files can be cleaned up manually
                    log.warn("Failed to delete vehicle image: {}", imageUrl, e);
                }
            }
        }
    }

    @CacheEvict(value = "vehicle", key = "#vehicleId")
    public void recordHeartbeat(Long vehicleId) {
        DeliveryVehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", vehicleId));

        vehicle.setIsOnline(true);
        vehicle.setLastHeartbeat(LocalDateTime.now());
        vehicleRepository.save(vehicle);
    }

    public List<SimulatorVehicleConfigDTO> getVehiclesForSimulator(Long startId, Long endId) {
        log.info("Fetching vehicles for simulator: {} to {}", startId, endId);

        List<DeliveryVehicle> vehicles = vehicleRepository.findByIdBetween(startId, endId);

        return vehicles.stream().map(this::convertToSimulatorConfig).collect(Collectors.toList());
    }

    private SimulatorVehicleConfigDTO convertToSimulatorConfig(DeliveryVehicle vehicle) {
        return SimulatorVehicleConfigDTO.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .build();
    }

    private DeliveryVehicleDTO convertToDTO(DeliveryVehicle vehicle) {
        DeliveryVehicleDTO dto = new DeliveryVehicleDTO();
        dto.setId(vehicle.getId());
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setImages(new HashSet<>(vehicle.getImages()));
        dto.setWeightLimit(vehicle.getWeightLimit());
        dto.setCreatedAt(vehicle.getCreatedAt());
        dto.setUpdatedAt(vehicle.getUpdatedAt());
        dto.setMakeId(vehicle.getMake().getId());
        dto.setModelId(vehicle.getModel().getId());
        dto.setMakeName(vehicle.getMake().getName());
        dto.setModelName(vehicle.getModel().getName());
        dto.setVersion(vehicle.getVersion());
        dto.setLastHeartbeat(vehicle.getLastHeartbeat());
        dto.setLastLatitude(vehicle.getLastLatitude());
        dto.setLastLongitude(vehicle.getLastLongitude());
        dto.setLastLocationReadingAt(vehicle.getLastLocationReadingAt());
        dto.setIsOnline(vehicle.getIsOnline());
        return dto;
    }
}
