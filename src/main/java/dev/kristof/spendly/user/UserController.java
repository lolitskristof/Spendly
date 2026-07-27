package dev.kristof.spendly.user;

import dev.kristof.spendly.auth.CurrentUserService;
import dev.kristof.spendly.user.dto.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final CurrentUserService currentUserService;

    public UserController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        User user = currentUserService.getLoggedInUser();
        return new UserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole());
    }

}
