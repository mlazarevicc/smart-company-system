package ftn.siit.nvt.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class RegisterRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotNull(message = "Profile image is required")
    private MultipartFile profileImage;

    public @NotNull(message = "Profile image is required") MultipartFile getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(@NotNull(message = "Profile image is required") MultipartFile profileImage) {
        this.profileImage = profileImage;
    }

    public @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters long") String getPassword() {
        return password;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters long") String password) {
        this.password = password;
    }

    public @NotBlank(message = "Username is required") String getUsername() {
        return username;
    }

    public void setUsername(@NotBlank(message = "Username is required") String username) {
        this.username = username;
    }

    public @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String getEmail() {
        return email;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email) {
        this.email = email;
    }

    public @NotBlank(message = "Last name is required") String getLastName() {
        return lastName;
    }

    public void setLastName(@NotBlank(message = "Last name is required") String lastName) {
        this.lastName = lastName;
    }

    public @NotBlank(message = "First name is required") String getFirstName() {
        return firstName;
    }

    public void setFirstName(@NotBlank(message = "First name is required") String firstName) {
        this.firstName = firstName;
    }

    public RegisterRequest(String firstName, String lastName, String email, String username, String password, MultipartFile profileImage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.profileImage = profileImage;
    }

    public RegisterRequest() {
    }
}