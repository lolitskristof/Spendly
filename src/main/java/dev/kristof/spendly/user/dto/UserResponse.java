package dev.kristof.spendly.user.dto;

import dev.kristof.spendly.user.UserRole;
import lombok.Getter;

@Getter
public class UserResponse {
    private Long id;
    private String displayName;
    private String email;
    private UserRole role;

    public UserResponse(Long id, String displayName, String email, UserRole role) {
        this.id = id;
        this.displayName = displayName;
        this.email = email;
        this.role = role;
    }
}
