package ftn.siit.nvt.controller;

import ftn.siit.nvt.dto.user.BlockManagerRequest;
import ftn.siit.nvt.dto.user.ChangePasswordRequest;
import ftn.siit.nvt.dto.user.ManagerDTO;
import ftn.siit.nvt.dto.user.RegisterManagerRequest;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.User;
import ftn.siit.nvt.repository.ManagerRepository;
import ftn.siit.nvt.service.AuthService;
import ftn.siit.nvt.service.ManagerService;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/managers")
@Validated
public class ManagerController {

    private final ManagerService managerService;
    private final ManagerRepository managerRepository;
    private final AuthService authService;

    public ManagerController(ManagerService managerService, ManagerRepository managerRepository, AuthService authService) {
        this.managerService = managerService;
        this.managerRepository = managerRepository;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_SUPERMANAGER')")
    public ResponseEntity<PaginatedResponse<ManagerDTO>> getManagers(
            @RequestParam(required = false, name = "q") String search,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        log.info("GET /api/managers - search='{}', isBlocked={}, page={}, size={}", search, isBlocked, page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        String actualSortProperty = switch (sortBy.toLowerCase()) {
            case "firstname" -> "first_name";
            case "lastname" -> "last_name";
            case "username" -> "username";
            case "email" -> "email";
            case "createdat" -> "created_at";
            default -> "created_at";
        };

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, actualSortProperty));

        PaginatedResponse<ManagerDTO> managers = managerService.getManagers(search, isBlocked, pageable);
        return ResponseEntity.ok(managers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SUPERMANAGER')")
    public ResponseEntity<ManagerDTO> getManagerById(@PathVariable Long id) {
        log.info("GET /api/managers/{}", id);

        ManagerDTO manager = managerService.getManagerById(id);
        return ResponseEntity.ok(manager);
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ROLE_SUPERMANAGER')")
    public ResponseEntity<ManagerDTO> registerManager(
            @ModelAttribute @Valid RegisterManagerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("POST /api/managers/register - username='{}', by super-admin: {}",
                request.getUsername(), currentUser.getUsername());

        Manager currentManager = managerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        ManagerDTO newManager = managerService.registerManager(request, currentManager);

        return ResponseEntity.status(HttpStatus.CREATED).body(newManager);
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ROLE_SUPERMANAGER')")
    public ResponseEntity<ManagerDTO> blockManager(
            @PathVariable Long id,
            @Valid @RequestBody BlockManagerRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("PUT /api/managers/{}/block - by manager: {}", id, currentUser.getUsername());

        Manager currentManager = managerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        ManagerDTO blockedManager = managerService.blockManager(id, request, currentManager);

        return ResponseEntity.ok(blockedManager);
    }

    @PutMapping("/{id}/unblock")
    @PreAuthorize("hasRole('ROLE_SUPERMANAGER')")
    public ResponseEntity<ManagerDTO> unblockManager(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser
    ) {
        log.info("PUT /api/managers/{}/unblock - by manager: {}", id, currentUser.getUsername());

        Manager currentManager = managerRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        ManagerDTO unblockedManager = managerService.unblockManager(id, currentManager);

        return ResponseEntity.ok(unblockedManager);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        authService.changePassword(
                userDetails.getUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        response.put("resetPassword", true);

        return ResponseEntity.ok(response);
    }
}