package ftn.siit.nvt.service;

import ftn.siit.nvt.dto.user.LoginResponse;
import ftn.siit.nvt.dto.user.RegisterRequest;
import ftn.siit.nvt.dto.user.UserDTO;
import ftn.siit.nvt.exception.ResourceNotFoundException;
import ftn.siit.nvt.model.Customer;
import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.User;
import ftn.siit.nvt.model.VerificationToken;
import ftn.siit.nvt.model.enums.UserRole;
import ftn.siit.nvt.repository.CustomerRepository;
import ftn.siit.nvt.repository.ManagerRepository;
import ftn.siit.nvt.repository.TokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ManagerRepository managerRepository;

    public AuthService(CustomerRepository customerRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, EmailService emailService,
                       ManagerRepository managerRepository) {
        this.customerRepository = customerRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.managerRepository = managerRepository;
    }

    @Value("${file.storage.location}")
    private String uploadDir;

    @Transactional
    public UserDTO registerCustomer(RegisterRequest request) throws IOException {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        if (customerRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already in use");
        }

        String imageFilename = saveProfileImage(request.getProfileImage());

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setUsername(request.getUsername());
        customer.setEmail(request.getEmail());
        customer.setPassword(passwordEncoder.encode(request.getPassword()));
        customer.setProfileImage(imageFilename);

        customer.setRole(UserRole.CUSTOMER);
        customer.setActive(false);

        Customer savedCustomer = customerRepository.save(customer);

        VerificationToken token = new VerificationToken(customer);
        tokenRepository.save(token);

        emailService.sendVerificationEmail(customer.getEmail(), token.getToken());
        return new UserDTO(savedCustomer);
    }

    private String saveProfileImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Profile image is empty");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    public void verifyAccount(String tokenString) {
        VerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.isExpired()) {
            throw new RuntimeException("Token has expired");
        }

        Customer customer = token.getCustomer();
        if (customer.getActive()) {
            throw new RuntimeException("Account is already active");
        }

        customer.setActive(true);
        customerRepository.save(customer);

        tokenRepository.delete(token);
    }

    @Transactional
    @CacheEvict(value = {"manager", "managers_page"}, allEntries = true)
    public void changePassword(String username, String oldPassword, String newPassword) {
        Manager manager = managerRepository.findByUsername(username)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Manager", "username", username)
                );

        if (!passwordEncoder.matches(oldPassword, manager.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        manager.setPassword(passwordEncoder.encode(newPassword));
        manager.setResetPassword(true);

        managerRepository.save(manager);
    }

    public LoginResponse buildLoginResponse(String email, User user, String jwt, int expiresIn) {
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("ROLE_USER");

        LoginResponse.LoginResponseBuilder builder = LoginResponse.builder()
                .jwt(jwt)
                .expiresIn(expiresIn)
                .role(role);

        // Handle Manager/SUPERMANAGER
        if (role.equals("ROLE_MANAGER") || role.equals("ROLE_SUPERMANAGER")) {
            return buildManagerResponse(user, builder);
        }

        // Handle Customer
        if (role.equals("ROLE_CUSTOMER")) {
            return buildCustomerResponse(user, builder);
        }

        // Default response for unknown roles
        return builder.requiresPasswordReset(false).build();
    }

    private LoginResponse buildManagerResponse(User user, LoginResponse.LoginResponseBuilder builder) {
        Manager manager = (Manager) user;

        if (manager.getBlocked()) {
            throw new RuntimeException("Your account has been blocked. Contact administrator.");
        }

        return builder
                .userId(manager.getId().toString())
                .username(manager.getUsername())
                .role(manager.getSupermanager() ? "SUPERMANAGER" : "MANAGER")
                .requiresPasswordReset(!manager.getResetPassword())
                .build();
    }

    private LoginResponse buildCustomerResponse(User user, LoginResponse.LoginResponseBuilder builder) {
        Customer customer = (Customer) user;
        return builder
                .userId(customer.getId().toString())
                .username(customer.getUsername())
                .requiresPasswordReset(false) // Customers don't need mandatory password reset
                .build();
    }
}