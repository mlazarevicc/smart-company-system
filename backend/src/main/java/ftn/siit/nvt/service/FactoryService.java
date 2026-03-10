package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.factory.*;
import ftn.siit.nvt.dto.product.ProductDTO;
import ftn.siit.nvt.dto.product.SimulatorProductConfigDTO;
import ftn.siit.nvt.exception.ConcurrentModificationException;
import ftn.siit.nvt.model.*;
import ftn.siit.nvt.repository.*;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FactoryService {

    private final FactoryRepository factoryRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    /* ===================== CREATE ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "factories_page", allEntries = true)
    public FactoryDTO createFactory(CreateFactoryRequest request, MultipartFile[] images, Manager creator) {
        if (factoryRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Factory with name already exists: " + request.getName());
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getCountryId()));

        Factory factory = new Factory();
        factory.setName(request.getName());
        factory.setAddress(request.getAddress());
        factory.setCity(city);
        factory.setCountry(country);
        factory.setLatitude(request.getLatitude());
        factory.setLongitude(request.getLongitude());
        factory.setCreatedBy(creator);
        factory.setIsOnline(false);

        List<String> uploadedImages = new ArrayList<>();
        try {
            if (images != null) {
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imageUrl = fileStorageService.saveFactoryImage(image, factory.getName());
                        factory.getImageUrls().add(imageUrl);
                        uploadedImages.add(imageUrl);
                    }
                }
            }

            if (factory.getImageUrls().isEmpty()) {
                factory.getImageUrls().add(fileStorageService.getDefaultFactoryImageUrl());
            }

            for (Long productId : request.getProductIds()) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
                factory.getProducts().add(product);
            }

            return convertToDTO(factoryRepository.save(factory));

        } catch (Exception e) {
            for (String url : uploadedImages) {
                try { fileStorageService.deleteByUrl(url); } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    /* ===================== UPDATE ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @Caching(evict = {@CacheEvict(value = "factory", key = "#id"), @CacheEvict(value = "factories_page", allEntries = true)})
    public FactoryDTO updateFactory(Long id, UpdateFactoryRequest request, MultipartFile[] images) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", id));

        if (!factory.getVersion().equals(request.getVersion())) {
            throw new ConcurrentModificationException("Factory", id, request.getVersion(), factory.getVersion());
        }

        updateBasicFactoryData(factory, request);

        List<String> oldImagesToCleanup = new ArrayList<>();
        List<String> newUploadedImages = new ArrayList<>();

        if (images != null && images.length > 0) {
            oldImagesToCleanup.addAll(factory.getImageUrls());

            try {
                factory.getImageUrls().clear();
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imageUrl = fileStorageService.saveFactoryImage(image, factory.getName());
                        factory.getImageUrls().add(imageUrl);
                        newUploadedImages.add(imageUrl);
                    }
                }

                if (factory.getImageUrls().isEmpty() && oldImagesToCleanup.isEmpty()) {
                    factory.getImageUrls().add(fileStorageService.getDefaultFactoryImageUrl());
                }
            } catch (Exception e) {
                newUploadedImages.forEach(url -> {
                    try { fileStorageService.deleteByUrl(url); } catch (Exception ignored) {}
                });
                throw new RuntimeException("Greška prilikom čuvanja slika: " + e.getMessage());
            }
        }

        Factory updatedFactory = factoryRepository.save(factory);

        if (!oldImagesToCleanup.isEmpty()) {
            for (String oldUrl : oldImagesToCleanup) {
                if (!oldUrl.equals(fileStorageService.getDefaultFactoryImageUrl())) {
                    try { fileStorageService.deleteByUrl(oldUrl); } catch (Exception ignored) {}
                }
            }
        }

        return convertToDTO(updatedFactory);
    }

    private void updateBasicFactoryData(Factory factory, UpdateFactoryRequest request) {
        if (request.getName() != null && !request.getName().isBlank()) factory.setName(request.getName());
        if (request.getAddress() != null && !request.getAddress().isBlank()) factory.setAddress(request.getAddress());
        if (request.getLatitude() != null) factory.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) factory.setLongitude(request.getLongitude());

        if (request.getCityId() != null) {
            City city = cityRepository.findById(request.getCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
            factory.setCity(city);
        }

        if (request.getCountryId() != null) {
            Country country = countryRepository.findById(request.getCountryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getCountryId()));
            factory.setCountry(country);
        }

        if (request.getProductIds() != null) {
            factory.getProducts().clear();
            for (Long productId : request.getProductIds()) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
                factory.getProducts().add(product);
            }
        }
    }

    /* ===================== HARD DELETE ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @Caching(evict = {@CacheEvict(value = "factory", key = "#id"), @CacheEvict(value = "factories_page", allEntries = true)})
    public void deleteFactory(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", id));

        for (Product product : factory.getProducts()) {
            product.getFactories().remove(factory);
        }

        List<String> imagesToDelete = new ArrayList<>(factory.getImageUrls());

        factoryRepository.delete(factory);
        factoryRepository.flush();

        // Čišćenje fajlova nakon uspješnog brisanja iz baze
        for (String imageUrl : imagesToDelete) {
            if (!imageUrl.equals(fileStorageService.getDefaultFactoryImageUrl())) {
                try {
                    fileStorageService.deleteByUrl(imageUrl);
                } catch (Exception e) {
                    log.error("Upozorenje: Slika {} nije uspesno izbrisana sa diska nakon brisanja fabrike", imageUrl, e);
                }
            }
        }
    }

    /* ===================== HEARTBEAT ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "factory", key = "#factoryId")
    public void recordHeartbeat(Long factoryId) {
        try {
            Factory factory = factoryRepository.findById(factoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));

            factory.setIsOnline(true);
            factory.setLastHeartbeat(LocalDateTime.now());
            factoryRepository.save(factory);
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            log.debug("Heartbeat konflikt za fabriku {}. Ignorišem do sledećeg tucanja.", factoryId);
        }
    }

    /* ===================== READ-ONLY METODE ===================== */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(value = "factories_page", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<FactoryDTO> getAllFactories(Pageable pageable) {
        Page<Factory> factories = factoryRepository.findAll(pageable);
        List<FactoryDTO> dtos = factories.getContent().stream().map(this::convertToDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, factories.getTotalElements());
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(value = "factory", key = "#id")
    public FactoryDTO getFactoryById(Long id) {
        Factory factory = factoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", id));
        return convertToDTO(factory);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(value = "factories_page", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString() + " +
            "'-' + (#filter.query() != null ? #filter.query() : 'none') + " +
            "'-' + (#filter.countryId() != null ? #filter.countryId() : 'none') + " +
            "'-' + (#filter.cityId() != null ? #filter.cityId() : 'none') + " +
            "'-' + (#filter.online() != null ? #filter.online() : 'none')")
    public PaginatedResponse<FactoryDTO> filterFactories(FactoryFilterRequest filter, Pageable pageable) {
        String q = (filter.query() == null || filter.query().isBlank()) ? null : filter.query();

        Page<Factory> factoryPage = factoryRepository.searchAdvanced(
                q, filter.countryId(), filter.cityId(), filter.online(), pageable
        );

        Page<FactoryDTO> dtoPage = factoryPage.map(this::convertToDTO);
        return new PaginatedResponse<>(dtoPage);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<SimulatorFactoryConfigDTO> getFactoriesForSimulator(Long startId, Long endId) {
        if (endId - startId > 5000) {
            throw new IllegalArgumentException("Range too large. Maximum 5000 factories per request.");
        }
        return factoryRepository.findByIdBetween(startId, endId).stream()
                .map(this::convertToSimulatorConfig)
                .collect(Collectors.toList());
    }

    /* ===================== POMOĆNE METODE ===================== */
    private SimulatorFactoryConfigDTO convertToSimulatorConfig(Factory factory) {
        List<SimulatorProductConfigDTO> productConfigs = factory.getProducts().stream()
                .map(product -> SimulatorProductConfigDTO.builder()
                        .productId(product.getId())
                        .minQuantity(determineMinQuantity(product))
                        .maxQuantity(determineMaxQuantity(product))
                        .build())
                .collect(Collectors.toList());

        return SimulatorFactoryConfigDTO.builder()
                .id(factory.getId())
                .name(factory.getName())
                .products(productConfigs)
                .build();
    }

    private Long determineMinQuantity(Product product) {
        return product.getWeight().compareTo(BigDecimal.valueOf(1.0)) > 0 ? 100L : 500L;
    }

    private Long determineMaxQuantity(Product product) {
        return product.getWeight().compareTo(BigDecimal.valueOf(1.0)) > 0 ? 500L : 2000L;
    }

    private FactoryDTO convertToDTO(Factory factory) {
        FactoryDTO dto = new FactoryDTO();
        dto.setId(factory.getId());
        dto.setName(factory.getName());
        dto.setAddress(factory.getAddress());
        dto.setCity(factory.getCity().getName());
        dto.setCountry(factory.getCountry().getName());
        dto.setLatitude(factory.getLatitude());
        dto.setLongitude(factory.getLongitude());
        dto.setIsOnline(factory.getIsOnline());
        dto.setLastHeartbeat(factory.getLastHeartbeat());
        dto.setCreatedAt(factory.getCreatedAt());
        dto.setUpdatedAt(factory.getUpdatedAt());
        dto.setVersion(factory.getVersion());

        if (factory.getImageUrls() != null) {
            dto.setImageUrls(new java.util.HashSet<>(factory.getImageUrls()));
        } else {
            dto.setImageUrls(new java.util.HashSet<>());
        }

        if (factory.getProducts() != null) {
            dto.setProducts(factory.getProducts().stream().map(this::convertProductToDTO).collect(Collectors.toList()));
        }
        return dto;
    }

    private ProductDTO convertProductToDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .sku(product.getSku())
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .weight(product.getWeight())
                .productImage(product.getImageUrl())
                .isAvailable(product.getIsAvailable())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .version(product.getVersion())
                .build();
    }
}