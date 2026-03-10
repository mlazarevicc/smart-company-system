package ftn.siit.nvt.utils;

import ftn.siit.nvt.model.Manager;
import ftn.siit.nvt.model.enums.UserRole;
import ftn.siit.nvt.repository.ManagerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class ManagerStartupService {

    private final ManagerRepository managerRepository;
    private final PasswordEncoder passwordEncoder;

    public ManagerStartupService(
            ManagerRepository managerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.managerRepository = managerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("════════════════════════════════════════════════════════");
        log.info("ManagerStartupService initialized...");
        log.info("════════════════════════════════════════════════════════");

        try {
            createSUPERMANAGERIfNotExists();
        } catch (Exception e) {
            log.error("Error creating super manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize super manager", e);
        }
    }

    private void createSUPERMANAGERIfNotExists() {
        Optional<Manager> existingAdmin = managerRepository.findByUsername("admin");

        if (existingAdmin.isPresent()) {
            log.info("Super-admin 'admin' already exists in database");
            return;
        }

        log.warn(" No users in database - creating super-admin...");

        // Generate random password (32 characters, alphanumeric + special)
        String randomPassword = generateRandomPassword();

        // Create new Manager
        Manager SUPERMANAGER = new Manager();
        SUPERMANAGER.setFirstName("Super");
        SUPERMANAGER.setLastName("Admin");
        SUPERMANAGER.setUsername("admin");
        SUPERMANAGER.setEmail("admin@smartmanufacturing.com");
        SUPERMANAGER.setPassword(passwordEncoder.encode(randomPassword));
        SUPERMANAGER.setRole(UserRole.MANAGER);
        SUPERMANAGER.setSupermanager(true);
        SUPERMANAGER.setBlocked(false);
        SUPERMANAGER.setActive(true);
        SUPERMANAGER.setResetPassword(false);
        SUPERMANAGER.setProfileImage("");
        SUPERMANAGER.setCreatedAt(LocalDateTime.now());

        // Save to database
        Manager savedAdmin = managerRepository.save(SUPERMANAGER);
        log.info("Super-admin successfully created!");
        log.info("👤 Username: {}", savedAdmin.getUsername());
        log.info("📧 Email: {}", savedAdmin.getEmail());

        // Save password to file
        savePasswordToFile(randomPassword);

        log.info("════════════════════════════════════════════════════════");
    }

    private String generateRandomPassword() {
        String uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()-_=+[]{}|:;<>,.?/";
        String all = uppercase + lowercase + digits + special;

        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(uppercase.charAt((int) (Math.random() * uppercase.length())));
        password.append(lowercase.charAt((int) (Math.random() * lowercase.length())));
        password.append(digits.charAt((int) (Math.random() * digits.length())));
        password.append(special.charAt((int) (Math.random() * special.length())));

        // Fill remaining 28 characters randomly
        for (int i = 4; i < 32; i++) {
            password.append(all.charAt((int) (Math.random() * all.length())));
        }

        return password.toString();
    }

    private void savePasswordToFile(String password) {
        try {
            // Create target directory if not exists
            File targetDir = new File("target");
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // Write password to file
            File passwordFile = new File("target/admin_password.txt");
            try (FileWriter writer = new FileWriter(passwordFile)) {
                writer.write("╔════════════════════════════════════════════════════════╗\n");
                writer.write("║  SMART MANUFACTURING PLATFORM - SUPER-ADMIN PASSWORD   ║\n");
                writer.write("║  Generated: " + LocalDateTime.now() + "\n");
                writer.write("║   KEEP THIS FILE SECURE - DELETE AFTER FIRST LOGIN  ║\n");
                writer.write("╚════════════════════════════════════════════════════════╝\n\n");
                writer.write("Username: admin\n");
                writer.write("Email: admin@smartmanufacturing.com\n");
                writer.write("Initial Password: " + password + "\n\n");
                writer.write("IMPORTANT INSTRUCTIONS:\n");
                writer.write("1. Login with username 'admin' and this password\n");
                writer.write("2. You will be FORCED to change the password on first login\n");
                writer.write("3. After password change, delete this file for security\n");
                writer.write("4. Password contains uppercase, lowercase, numbers, and special characters\n\n");
                writer.write("Do NOT share this password with anyone!\n");
            }

            log.info("🔑 Password saved to: target/admin_password.txt");
            log.info(" Delete this file after first login for security!");

        } catch (IOException e) {
            log.error("Error saving password to file: {}", e.getMessage());
            throw new RuntimeException("Failed to save admin password to file", e);
        }
    }
}