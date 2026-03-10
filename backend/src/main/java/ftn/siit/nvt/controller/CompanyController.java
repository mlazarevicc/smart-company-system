package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.company.ApprovedCompanyDTO;
import ftn.siit.nvt.dto.company.CompanyDTO;
import ftn.siit.nvt.dto.company.CreateCompanyRequest;
import ftn.siit.nvt.dto.company.UpdateCompanyRequest;
import ftn.siit.nvt.dto.factory.*;
import ftn.siit.nvt.model.Customer;
import ftn.siit.nvt.repository.CompanyRepository;
import ftn.siit.nvt.repository.CustomerRepository;
import ftn.siit.nvt.service.CompanyService;
import ftn.siit.nvt.utils.PaginatedResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final CustomerRepository customerRepository;
    private final CompanyService companyService;

    public CompanyController(CompanyRepository companyRepository, CustomerRepository customerRepository, CompanyService companyService) {
        this.companyRepository = companyRepository;
        this.customerRepository = customerRepository;
        this.companyService = companyService;
    }

    // TODO: see if this works for you
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/my-companies")
    public ResponseEntity<List<ApprovedCompanyDTO>> getMyCompanies(Authentication auth) {
        Customer customer = customerRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        return ResponseEntity.ok(companyService.getAllApprovedCompaniesForCustomer(customer));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CompanyDTO> createCompany(
            @RequestPart("data") @Valid CreateCompanyRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "proofOfOwnership", required = false) MultipartFile[] proofOfOwnership,
            @AuthenticationPrincipal Customer currentCustomer
    ) {
        CompanyDTO created = companyService.createCompany(request, images, proofOfOwnership, currentCustomer);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<PaginatedResponse<CompanyDTO>> getAllCompaniesForCustomer(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort,
            @AuthenticationPrincipal Customer currentCustomer
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(sort).descending());
        PaginatedResponse<CompanyDTO> companies = companyService.getAllCompaniesForCustomer(pageable, currentCustomer);
        return ResponseEntity.ok(companies);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/filter")
    public Page<CompanyDTO> filterCompaniesForCustomer(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long countryId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort, // trenutno ignorisano
            @AuthenticationPrincipal Customer currentCustomer
    ) {
        Pageable pageable = PageRequest.of(page, size); // bez sortBy, sort rešava SQL

        return companyService.filterCompaniesForCustomer(query, countryId, cityId, currentCustomer, pageable);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/pending")
    public ResponseEntity<Page<CompanyDTO>> getAllPendingCompanies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(sort).descending());
        Page<CompanyDTO> companies = companyService.getAllPendingCompanies(pageable);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getCompanyById(@PathVariable Long id) {
        CompanyDTO company = companyService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<CompanyDTO> approveCompany(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyRequest request
    ) {
        CompanyDTO updated = companyService.approveCompany(id, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<CompanyDTO> rejectCompany(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCompanyRequest request
    ) {
        CompanyDTO updated = companyService.rejectCompany(id, request);
        return ResponseEntity.ok(updated);
    }
}
