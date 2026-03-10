package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.company.ApprovedCompanyDTO;
import ftn.siit.nvt.dto.company.CompanyDTO;
import ftn.siit.nvt.dto.company.CreateCompanyRequest;
import ftn.siit.nvt.dto.company.UpdateCompanyRequest;
import ftn.siit.nvt.dto.factory.*;
import ftn.siit.nvt.exception.ConcurrentModificationException;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.*;
import ftn.siit.nvt.model.enums.CompanyStatus;
import ftn.siit.nvt.repository.*;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    @CacheEvict(value = "customer_companies", key = "#creator.id")
    public CompanyDTO createCompany(CreateCompanyRequest request,
                                    MultipartFile[] images,
                                    MultipartFile[] proofOfOwnership,
                                    Customer creator) {
        if (companyRepository.findByName(request.getName()).isPresent()) {
            throw new IllegalArgumentException("Company with name already exists: " + request.getName());
        }

        City city = cityRepository.findById(request.getCityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", "id", request.getCityId()));
        Country country = countryRepository.findById(request.getCountryId())
                .orElseThrow(() -> new ResourceNotFoundException("Country", "id", request.getCountryId()));

        Company company = new Company();
        company.setName(request.getName());
        company.setAddress(request.getAddress());
        company.setCity(city);
        company.setCountry(country);
        company.setLatitude(request.getLatitude());
        company.setLongitude(request.getLongitude());
        company.setOwner(creator);
        company.setStatus(CompanyStatus.PENDING);

        if (images != null && images.length > 0) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    try {
                        String imageUrl = fileStorageService.saveCompanyImage(image, company.getName());
                        company.getImages().add(imageUrl);
                    } catch (Exception e) {
                        log.warn("Failed to upload image: {}", e.getMessage());
                    }
                }
            }
        }

        if (company.getImages().isEmpty()) {
            company.getImages().add(fileStorageService.getDefaultCompanyImageUrl());
        }

        if (proofOfOwnership == null || proofOfOwnership.length == 0) {
            throw new IllegalArgumentException("Proof of ownership is required.");
        }

        for (MultipartFile file : proofOfOwnership) {
            if (file.isEmpty()) continue;

            String contentType = file.getContentType();

            boolean isImage = contentType != null && contentType.startsWith("image/");
            boolean isPdf = "application/pdf".equals(contentType);

            if (!isImage && !isPdf) {
                throw new IllegalArgumentException("Proof of ownership must be PDF or image.");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Proof of ownership file exceeds 5MB.");
            }

            try {
                String proofUrl = fileStorageService.saveCompanyProofOfOwnership(file, company.getName());
                company.getProofOfOwnership().add(proofUrl);
            } catch (Exception e) {
                log.error("Failed to upload ownership proof: {}", e.getMessage());
                throw new RuntimeException("Failed to store proof of ownership.");
            }
        }

        log.info("Creating company: name={}, cityId={}, city={}, countryId={}, country={}",
                company.getName(),
                request.getCityId(),
                company.getCity() != null ? company.getCity().getId() : "NULL",
                request.getCountryId(),
                company.getCountry() != null ? company.getCountry().getId() : "NULL"
        );

        Company saved = companyRepository.save(company);
        return convertToDTO(saved);
    }

    @Cacheable(value = "customer_companies", key = "#currentCustomer.id")
    public PaginatedResponse<CompanyDTO> getAllCompaniesForCustomer(Pageable pageable, Customer currentCustomer) {
        return new PaginatedResponse<>(companyRepository.findAllByOwnerAndStatusIn(currentCustomer, List.of(CompanyStatus.PENDING, CompanyStatus.APPROVED), pageable).map(this::convertToDTO));
    }

    public Page<CompanyDTO> getAllPendingCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAllByStatus(CompanyStatus.PENDING, pageable);
        List<CompanyDTO> dtos = companies.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, companies.getTotalElements());
    }

    public List<ApprovedCompanyDTO> getAllApprovedCompaniesForCustomer(Customer currentCustomer) {
        List<Company> companies = companyRepository.findAllByOwnerAndStatus(currentCustomer, CompanyStatus.APPROVED);
        return companies
                .stream()
                .map(this::convertToApprovedDTO)
                .toList();
    }

    @Cacheable(value = "companies", key = "#id")
    public CompanyDTO getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));
        return convertToDTO(company);
    }

    @Transactional(readOnly = true)
    public Page<CompanyDTO> filterCompaniesForCustomer(String query, Long countryId, Long cityId, Customer currentCustomer, Pageable pageable) {
        String q = (query == null || query.isBlank()) ? null : query;

        int limit = pageable.getPageSize();
        int offset = (int) pageable.getOffset();

        List<Company> companies = companyRepository.searchAdvanced(
                q,
                countryId,
                cityId,
                currentCustomer.getId(),
                limit,
                offset
        );

        long total = companyRepository.countAdvanced(
                q,
                countryId,
                cityId,
                currentCustomer.getId()
        );

        List<CompanyDTO> dtos = companies.stream()
                .map(this::convertToDTO)
                .toList();

        return new PageImpl<>(dtos, pageable, total);
    }

    @CacheEvict(value = "companies", key = "#id")
    public CompanyDTO approveCompany(Long id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        if (!company.getStatus().equals(CompanyStatus.PENDING)) {
            throw new ConcurrentModificationException(
                    "Company",
                    id,
                    0L,
                    1L
            );
        }

        String customerEmail = company.getOwner().getEmail();
        emailService.sendCompanyDecisionEmail(customerEmail, request.getReason(), company.getName(), "approved");

        company.setStatus(CompanyStatus.APPROVED);
        companyRepository.save(company);

        return convertToDTO(company);
    }

    @CacheEvict(value = "companies", key = "#id")
    public CompanyDTO rejectCompany(Long id, UpdateCompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", id));

        if (!company.getStatus().equals(CompanyStatus.PENDING)) {
            throw new ConcurrentModificationException(
                    "Company",
                    id,
                    0L,
                    1L
            );
        }

        String customerEmail = company.getOwner().getEmail();
        emailService.sendCompanyDecisionEmail(customerEmail, request.getReason(), company.getName(), "rejected");

        company.setStatus(CompanyStatus.REJECTED);
        companyRepository.save(company);

        return convertToDTO(company);
    }

    private CompanyDTO convertToDTO(Company company) {
        CompanyDTO dto = new CompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setAddress(company.getAddress());
        dto.setCity(company.getCity().getName());
        dto.setCountry(company.getCountry().getName());
        dto.setLatitude(company.getLatitude());
        dto.setLongitude(company.getLongitude());
        dto.setImages(new HashSet<>(company.getImages()));
        dto.setStatus(company.getStatus().equals(CompanyStatus.APPROVED));
        dto.setProofOfOwnershipUrls(new HashSet<>(company.getProofOfOwnership()));
        dto.setOwnerName(company.getOwner().getFirstName() + " " + company.getOwner().getLastName());

        return dto;
    }

    private ApprovedCompanyDTO convertToApprovedDTO(Company company) {
        ApprovedCompanyDTO dto = new ApprovedCompanyDTO();
        dto.setId(company.getId());
        dto.setName(company.getName());

        return dto;
    }
}
