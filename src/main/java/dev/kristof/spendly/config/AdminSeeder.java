package dev.kristof.spendly.config;

import dev.kristof.spendly.user.User;
import dev.kristof.spendly.user.UserRepository;
import dev.kristof.spendly.user.UserRole;
import dev.kristof.spendly.user.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class AdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;

    public AdminSeeder(
            UserRepository userRepository,
            UserService userService,
            PasswordEncoder passwordEncoder,
            @Value("${SEED_ADMIN_PASSWORD:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email = "admin@admin.com";

        if (adminPassword.isBlank()) {
            throw new IllegalStateException("SEED_ADMIN_PASSWORD is required");
        }

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = userService.createUser(
                "Admin",
                email,
                passwordEncoder.encode(adminPassword)
        );

        admin.setRole(UserRole.ADMIN);
        userRepository.save(admin);
    }
}