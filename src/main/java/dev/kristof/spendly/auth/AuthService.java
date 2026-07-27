package dev.kristof.spendly.auth;

import dev.kristof.spendly.auth.dto.LoginRequest;
import dev.kristof.spendly.auth.dto.LoginResponse;
import dev.kristof.spendly.auth.dto.RegisterRequest;
import dev.kristof.spendly.auth.dto.RegisterResponse;
import dev.kristof.spendly.common.exception.InvalidCredentialsException;
import dev.kristof.spendly.user.User;
import dev.kristof.spendly.user.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest registerRequest) {
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = userService.createUser(registerRequest.getDisplayName(), registerRequest.getEmail(), hashedPassword);
        return new RegisterResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole());
    }

    public LoginResponse login(LoginRequest loginRequest) {
        User user = userService.getUserByEmail(loginRequest.getEmail());
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new LoginResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRole(), jwtService.generateJwt(user));
        }

        throw new InvalidCredentialsException("Invalid credentials");
    }
}
