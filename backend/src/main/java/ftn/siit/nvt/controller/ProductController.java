package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.product.CreateProductRequest;
import ftn.siit.nvt.dto.product.ProductDTO;
import ftn.siit.nvt.dto.product.UpdateProductRequest;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.User;
import ftn.siit.nvt.model.enums.ProductCategory;
import ftn.siit.nvt.repository.ManagerRepository;
import ftn.siit.nvt.service.ProductService;
import ftn.siit.nvt.utils.PaginatedResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
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

@RestController
@RequestMapping("/api/products")
@Validated
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final ManagerRepository managerRepository;

    public ProductController(ProductService productService,
                             ManagerRepository managerRepository) {
        this.productService = productService;
        this.managerRepository = managerRepository;
    }

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDTO> createProduct(
            @RequestPart("data") @Valid CreateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal User currentUser
    ) {
        Manager manager = managerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        ProductDTO product = productService.createProduct(request, image, manager);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    /* ===================== READ ===================== */

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ProductDTO>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        size = Math.min(size, 100);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String actualSortProperty = switch (sortBy.toLowerCase()) {
            case "price" -> "price";
            case "sku" -> "sku";
            case "category" -> "category";
            case "weight" -> "weight";
            case "created_at", "createdat" -> "created_at";
            case "updated_at", "updatedat" -> "updated_at";
            default -> "name";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, actualSortProperty));

        return ResponseEntity.ok(
                productService.searchProducts(search, category, available, pageable)
        );
    }

    /* ===================== UPDATE ===================== */

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestPart("data") @Valid UpdateProductRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.ok(
                productService.updateProduct(id, request, image)
        );
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}