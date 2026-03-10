package ftn.siit.nvt.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    @Value("${file.storage.location}")
    private String storageRoot;

    @Value("${file.storage.public-base-url}")
    private String publicBaseUrl;

    @Value("${file.storage.default-user-image}")
    private String defaultUserImage;

    @Value("${file.storage.default-product-image}")
    private String defaultProductImage;

    public String getDefaultUserImageUrl() {
        return publicBaseUrl + defaultUserImage;
    }

    public String getDefaultProductImageUrl() {
        return publicBaseUrl + defaultProductImage;
    }
    public String getDefaultFactoryImageUrl() {
        return publicBaseUrl + defaultProductImage;
    }
    public String getDefaultWarehouseImageUrl() {
        return publicBaseUrl + defaultProductImage;
    }
    public String getDefaultVehicleImageUrl() { return publicBaseUrl + defaultProductImage; }
    public String getDefaultCompanyImageUrl() { return publicBaseUrl + defaultProductImage; }

    public String saveFactoryImage(MultipartFile file, String factoryName) {
        String random = UUID.randomUUID().toString();
        return saveImage(file, "factory/" + factoryName, "image_" + random);
    }

    public String saveCompanyImage(MultipartFile file, String companyName) {
        String random = UUID.randomUUID().toString();
        return saveImage(file, "company/" + companyName, "image_" + random);
    }

    public String saveProductImage(MultipartFile file, String sku) {
        return saveImage(file, "products/" + sku, "main");
    }

    public String saveManagerAvatar(MultipartFile file, String username) {
        return saveImage(file, "managers/" + username, "avatar");
    }

    public String saveConsumerAvatar(MultipartFile file, String username) {
        return saveImage(file, "consumers/" + username, "avatar");
    }

    public String saveWarehouseImage(MultipartFile file, String warehouseName) {
        String random = UUID.randomUUID().toString();
        return saveImage(file, "warehouses/" + warehouseName, "image_" + random);
    }

    public String saveVehicleImage(MultipartFile file, String vehicleRegistrationNumber) {
        return saveImage(file, "vehicles/" + vehicleRegistrationNumber, "main");
    }

    public String saveCompanyProofOfOwnership(MultipartFile file, String companyName) {
        String random = UUID.randomUUID().toString();
        if ("application/pdf".equals(file.getContentType())) {
            return savePdf(file, "company/" + companyName + "/proof", "proof_" + random);
        } else {
            return saveImage(file, "company/" + companyName + "/proof", "proof_" + random);
        }

    }

    private String saveImage(MultipartFile file, String subDir, String baseName) {
        validate(file);

        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = baseName + "." + extension;

            Path dirPath = Paths.get(storageRoot, subDir);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return publicBaseUrl + "/" + subDir + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    private String savePdf(MultipartFile file, String subDir, String baseName) {
        validatePdf(file);

        try {
            String extension = getExtension(file.getOriginalFilename());
            String filename = baseName + "." + extension;

            Path dirPath = Paths.get(storageRoot, subDir);
            Files.createDirectories(dirPath);

            Path filePath = dirPath.resolve(filename);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return publicBaseUrl + "/" + subDir + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store pdf", e);
        }
    }

    public void deleteByUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            String relativePath = imageUrl.replace(publicBaseUrl + "/", "");
            Path filePath = Paths.get(storageRoot, relativePath);
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("Failed to delete image: " + imageUrl);
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported image type");
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large (max 5MB)");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported image type");
        }
    }

    private String getExtension(String filename) {
        String ext = StringUtils.getFilenameExtension(filename);
        return (ext != null) ? ext.toLowerCase() : "jpg";
    }
}