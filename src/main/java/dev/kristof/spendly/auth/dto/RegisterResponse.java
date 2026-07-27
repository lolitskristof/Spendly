package dev.kristof.spendly.auth.dto;

import dev.kristof.spendly.user.UserRole;
import lombok.Getter;

@Getter
public class RegisterResponse {
    private Long id;
    private String displayName;
    private String email;
    private UserRole role;

    public RegisterResponse(Long id, String displayName, String email, UserRole role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }
}
