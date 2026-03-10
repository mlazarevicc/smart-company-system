package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.factory.FactorySimpleDTO;
import ftn.siit.nvt.dto.product.CreateProductRequest;
import ftn.siit.nvt.dto.product.ProductDTO;
import ftn.siit.nvt.dto.product.UpdateProductRequest;
import ftn.siit.nvt.exception.ConcurrentModificationException;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Factory;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.Product;
import ftn.siit.nvt.model.enums.ProductCategory;
import ftn.siit.nvt.repository.FactoryProductQuantityRepository;
import ftn.siit.nvt.repository.FactoryRepository;
import ftn.siit.nvt.repository.ProductRepository;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final FactoryRepository factoryRepository;
    private final FactoryProductQuantityRepository fpqRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ProductService(ProductRepository productRepository,
                          FileStorageService fileStorageService,
                          FactoryRepository factoryRepository,
                          FactoryProductQuantityRepository fpqRepository) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
        this.factoryRepository = factoryRepository;
        this.fpqRepository = fpqRepository;
    }

    /* ===================== CREATE ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "products_page", allEntries = true)
    public ProductDTO createProduct(CreateProductRequest request, MultipartFile image, Manager createdBy) {
        String sku = generateSku(request.getCategory());

        if (productRepository.existsBySku(sku)) {
            throw new IllegalStateException("SKU already exists: " + sku);
        }

        Product product = new Product();
        product.setSku(sku);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setWeight(request.getWeight());
        product.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);
        product.setCreatedBy(createdBy);

        if (request.getFactoryIds() != null && !request.getFactoryIds().isEmpty()) {
            Set<Factory> factories = new HashSet<>();
            for (Long factoryId : request.getFactoryIds()) {
                Factory factory = factoryRepository.findById(factoryId)
                        .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId));
                factories.add(factory);
            }
            product.setFactories(factories);
        }

        String imageUrl = null;
        try {
            if (image != null && !image.isEmpty()) {
                imageUrl = fileStorageService.saveProductImage(image, product.getSku());
                product.setImageUrl(imageUrl);
            } else {
                product.setImageUrl(fileStorageService.getDefaultProductImageUrl());
            }

            Product saved = productRepository.save(product);
            return convertToDTO(saved);
        } catch (Exception e) {
            if (imageUrl != null && !imageUrl.equals(fileStorageService.getDefaultProductImageUrl())) {
                try {
                    fileStorageService.deleteByUrl(imageUrl);
                } catch (Exception fileEx) {
                    log.error("Greška pri brisanju zaostale slike", fileEx);
                }
            }
            throw e;
        }
    }

    /* ===================== UPDATE ===================== */

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = {"product", "products_page"}, key = "#id", allEntries = true)
    public ProductDTO updateProduct(Long id, UpdateProductRequest request, MultipartFile newImage) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (!product.getVersion().equals(request.getVersion())) {
            throw new ConcurrentModificationException(
                    "Product",
                    id,
                    request.getVersion(),
                    product.getVersion()
            );
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setWeight(request.getWeight());

        if (request.getIsAvailable() != null) {
            product.setIsAvailable(request.getIsAvailable());
        }

        if (request.getFactoryIds() != null) {
            Set<Factory> factories = request.getFactoryIds().stream()
                    .map(factoryId -> factoryRepository.findById(factoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Factory", "id", factoryId)))
                    .collect(Collectors.toSet());
            product.setFactories(factories);
        }

        String oldImageUrl = null;
        String newUploadedImageUrl = null;

        if (newImage != null && !newImage.isEmpty()) {
            oldImageUrl = product.getImageUrl();
            try {
                newUploadedImageUrl = fileStorageService.saveProductImage(newImage, product.getSku());
                product.setImageUrl(newUploadedImageUrl);
            } catch (Exception e) {
                throw new RuntimeException("Greška prilikom čuvanja slike proizvoda: " + e.getMessage());
            }
        }

        try {
            Product savedProduct = productRepository.save(product);

            if (oldImageUrl != null && !oldImageUrl.equals(fileStorageService.getDefaultProductImageUrl())) {
                try {
                    fileStorageService.deleteByUrl(oldImageUrl);
                } catch (Exception ignored) {}
            }

            return convertToDTO(savedProduct);

        } catch (Exception e) {
            if (newUploadedImageUrl != null) {
                try {
                    fileStorageService.deleteByUrl(newUploadedImageUrl);
                } catch (Exception ignored) {}
            }
            throw e;
        }
    }

    /* ===================== DELETE (SOFT DELETE) ===================== */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    @CacheEvict(value = {"product", "products_page"}, key = "#id", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(value = "product", key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return convertToDTO(product);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(value = "products_page", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString() + " +
                    "'-' + (#keyword != null ? #keyword : 'none') + '-' + (#category != null ? #category.name() : 'none') +" +
                    " '-' + (#isAvailable != null ? #isAvailable : 'none')")
    public PaginatedResponse<ProductDTO> searchProducts(String keyword, ProductCategory category, Boolean isAvailable, Pageable pageable) {

        String categoryName = category != null ? category.name() : null;

        Page<Product> page;
        if (keyword == null || keyword.trim().isEmpty()) {
            page = productRepository.searchWithoutKeyword(categoryName, isAvailable, pageable);
        } else {
            page = productRepository.searchWithKeyword(keyword, categoryName, isAvailable, pageable);
        }

        Page<ProductDTO> dtoPage = page.map(this::convertToDTO);
        return new PaginatedResponse<>(dtoPage);
    }

    public String generateSku(ProductCategory category) {
        String prefix = category.getSkuPrefix();
        String sequenceName = category.getSequenceName();
        Long nextVal = ((Number) entityManager
                .createNativeQuery("SELECT nextval('" + sequenceName + "')")
                .getSingleResult()).longValue();
        return String.format("%s-%03d", prefix, nextVal.intValue());
    }

    private ProductDTO convertToDTO(Product product) {
        Long totalQty = fpqRepository.getTotalQuantityForProduct(product.getId());

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
                .totalQuantity(totalQty)
                .factoryIds(product.getFactories().stream()
                        .map(Factory::getId)
                        .collect(Collectors.toSet()))
                .factories(product.getFactories().stream()
                        .map(factory -> FactorySimpleDTO.builder()
                                .id(factory.getId())
                                .name(factory.getName())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}