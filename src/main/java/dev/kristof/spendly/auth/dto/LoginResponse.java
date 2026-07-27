package dev.kristof.spendly.auth.dto;

import dev.kristof.spendly.user.UserRole;
import lombok.Getter;

@Getter
public class LoginResponse {
    private Long id;
    private String displayName;
    private String email;
    private UserRole role;
    private String token;

    public LoginResponse(Long id, String displayName, String email, UserRole role, String token) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
        this.token = token;
    }
}
