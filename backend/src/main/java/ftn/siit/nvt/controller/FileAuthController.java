package ftn.siit.nvt.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileAuthController {

    @GetMapping("/auth")
    public ResponseEntity<?> authenticateFileAccess(@RequestHeader("X-Original-URI") String originalUri, Authentication authentication) {
        if (originalUri.endsWith("product.jpg") || originalUri.endsWith("person-avatar.webp")) {
            return ResponseEntity.ok().build();
        }

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userRole = userDetails.getAuthorities().iterator().next().getAuthority();

        if (originalUri.contains("/uploads/factory/")) {
            if ("ROLE_MANAGER".equals(userRole)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        else if (originalUri.contains("/uploads/warehouses/")) {
            if ("ROLE_MANAGER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        else if (originalUri.contains("/uploads/products/")) {
            if ("ROLE_MANAGER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        else if (originalUri.contains("/uploads/company/")) {
            if ("ROLE_MANAGER".equals(userRole) || "ROLE_CUSTOMER".equals(userRole)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        else if (originalUri.contains("/uploads/vehicles/")) {
            if ("ROLE_MANAGER".equals(userRole)) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        else {
            return ResponseEntity.ok().build();
        }
    }
}
