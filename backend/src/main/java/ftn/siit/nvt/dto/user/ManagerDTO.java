package ftn.siit.nvt.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import ftn.siit.nvt.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManagerDTO implements Serializable {

    private Long id;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String username;

    private String email;

//    @JsonProperty("profile_image")
//    private String profileImage;

    private UserRole role;

    @JsonProperty("is_super_manager")
    private Boolean isSupermanager;

    @JsonProperty("is_blocked")
    private Boolean isBlocked;

    @JsonProperty("is_active")
    private Boolean active;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("display_name")
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    @JsonProperty(value = "status", access = JsonProperty.Access.READ_ONLY)
    public String getStatus() {
        if (isBlocked) return "BLOCKED";
        if (!active) return "INACTIVE";
        return "ACTIVE";
    }
}