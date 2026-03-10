package ftn.siit.nvt.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private int expiresIn;
    private String jwt;
    private boolean requiresPasswordReset;
    private String userId;
    private String username;
    private String role;
}
