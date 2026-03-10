package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.user.BlockManagerRequest;
import ftn.siit.nvt.dto.user.ManagerDTO;
import ftn.siit.nvt.dto.user.RegisterManagerRequest;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.enums.UserRole;
import ftn.siit.nvt.repository.ManagerRepository;
import ftn.siit.nvt.utils.FileStorageService;
import ftn.siit.nvt.utils.PaginatedResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileUploadService;

    public ManagerService(
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder,
            FileStorageService fileUploadService
    ) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileUploadService = fileUploadService;
    }

    @CacheEvict(value = "managers_page", allEntries = true)
    public ManagerDTO registerManager(RegisterManagerRequest request, Manager currentManager) {
        if (!currentManager.getSupermanager()) {
            throw new AccessDeniedException(
                    "Only supermanager can register new managers"
            );
        }

        // Check if username already exists
        if (managerRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        // Check if email already exists
        if (managerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }

        // Create new manager
        Manager manager = new Manager();
        manager.setFirstName(request.getFirstName());
        manager.setLastName(request.getLastName());
        manager.setUsername(request.getUsername());
        manager.setEmail(request.getEmail());
        manager.setPassword(passwordEncoder.encode(request.getPassword()));
        manager.setRole(UserRole.MANAGER);
        manager.setSupermanager(false);
        manager.setBlocked(false);
        manager.setActive(true);
        manager.setResetPassword(false);
        manager.setCreatedAt(LocalDateTime.now());

        // Upload profile image if provided
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            String imageUrl = fileUploadService.saveManagerAvatar(
                    request.getProfileImage(),
                    manager.getUsername()
            );
            manager.setProfileImage(imageUrl);
        } else {
            manager.setProfileImage(fileUploadService.getDefaultUserImageUrl());
        }

        Manager savedManager = managerRepository.save(manager);
        return convertToDTO(savedManager);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @Cacheable(
            value = "managers_page",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString() + '-' + (#query != null ? #query : 'none') + '-' + (#isBlocked != null ? #isBlocked : 'none')"
    )
    public PaginatedResponse<ManagerDTO> getManagers(String query, Boolean isBlocked, Pageable pageable) {

        Page<Manager> managers;
        if (query == null || query.trim().isEmpty()) {
            managers = managerRepository.searchWithoutKeyword(isBlocked, pageable);
        } else {
            managers = managerRepository.searchWithKeyword(query, isBlocked, pageable);
        }

        Page<ManagerDTO> dtoPage = managers.map(this::convertToDTO);
        return new PaginatedResponse<>(dtoPage);
    }

    @Cacheable(value = "manager", key = "#id")
    public ManagerDTO getManagerById(Long id) {
        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));

        return convertToDTO(manager);
    }

    @CacheEvict(value = {"manager", "managers_page"}, key = "#id", allEntries = true)
    public ManagerDTO blockManager(Long id, BlockManagerRequest request, Manager currentManager) {
        if (!currentManager.getSupermanager()) {
            throw new AccessDeniedException("Only supermanager can register new managers");
        }

        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));

        manager.setBlocked(true);
        Manager savedManager = managerRepository.save(manager);

        return convertToDTO(savedManager);
    }

    @CacheEvict(value = {"manager", "managers_page"}, key = "#id", allEntries = true)
    public ManagerDTO unblockManager(Long id, Manager currentManager) {
        if (!currentManager.getSupermanager()) {
            throw new AccessDeniedException("Only supermanager can unblock new managers");
        }

        Manager manager = managerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Manager", "id", id));

        manager.setBlocked(false);
        Manager savedManager = managerRepository.save(manager);

        return convertToDTO(savedManager);
    }

    private ManagerDTO convertToDTO(Manager manager) {
        return ManagerDTO.builder()
                .id(manager.getId())
                .firstName(manager.getFirstName())
                .lastName(manager.getLastName())
                .username(manager.getUsername())
                .email(manager.getEmail())
//                .profileImage(manager.getProfileImage())
                .role(manager.getRole())
                .isSupermanager(manager.getSupermanager())
                .isBlocked(manager.getBlocked())
                .active(manager.getActive())
                .createdAt(manager.getCreatedAt())
                .build();
    }
}