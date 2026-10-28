package dev.kristof.spendly.auth;

import dev.kristof.spendly.auth.dto.LoginRequest;
import dev.kristof.spendly.auth.dto.LoginResponse;
import dev.kristof.spendly.auth.dto.RegisterRequest;
import dev.kristof.spendly.auth.dto.RegisterResponse;
import dev.kristof.spendly.common.exception.InvalidCredentialsException;
import dev.kristof.spendly.user.User;
import dev.kristof.spendly.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldEncodePasswordAndCreateUser() {
        RegisterRequest request = mock(RegisterRequest.class);
        User user = mock(User.class);

        when(request.getDisplayName()).thenReturn("Test User");
        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("plainPassword");

        when(passwordEncoder.encode("plainPassword"))
                .thenReturn("hashedPassword");

        when(userService.createUser(
                "Test User",
                "test@example.com",
                "hashedPassword"
        )).thenReturn(user);

        when(user.getId()).thenReturn(1L);
        when(user.getDisplayName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");

        RegisterResponse response = authService.register(request);

        assertNotNull(response);

        verify(passwordEncoder).encode("plainPassword");

        verify(userService).createUser(
                "Test User",
                "test@example.com",
                "hashedPassword"
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void register_shouldNotCreateUserWhenPasswordEncodingFails() {
        RegisterRequest request = mock(RegisterRequest.class);

        when(request.getPassword()).thenReturn("plainPassword");

        when(passwordEncoder.encode("plainPassword"))
                .thenThrow(new IllegalStateException("Encoding failed"));

        assertThrows(
                IllegalStateException.class,
                () -> authService.register(request)
        );

        verifyNoInteractions(userService, jwtService);
    }

    @Test
    void login_shouldGenerateJwtWhenPasswordIsCorrect() {
        LoginRequest request = mock(LoginRequest.class);
        User user = mock(User.class);

        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("correctPassword");

        when(userService.getUserByEmail("test@example.com"))
                .thenReturn(user);

        when(user.getPassword()).thenReturn("hashedPassword");

        when(passwordEncoder.matches(
                "correctPassword",
                "hashedPassword"
        )).thenReturn(true);

        when(user.getId()).thenReturn(1L);
        when(user.getDisplayName()).thenReturn("Test User");
        when(user.getEmail()).thenReturn("test@example.com");

        when(jwtService.generateJwt(user))
                .thenReturn("test.jwt.token");

        LoginResponse response = authService.login(request);

        assertNotNull(response);

        verify(userService).getUserByEmail("test@example.com");

        verify(passwordEncoder).matches(
                "correctPassword",
                "hashedPassword"
        );

        verify(jwtService).generateJwt(user);
    }

    @Test
    void login_shouldRejectIncorrectPassword() {
        LoginRequest request = mock(LoginRequest.class);
        User user = mock(User.class);

        when(request.getEmail()).thenReturn("test@example.com");
        when(request.getPassword()).thenReturn("wrongPassword");

        when(userService.getUserByEmail("test@example.com"))
                .thenReturn(user);

        when(user.getPassword()).thenReturn("hashedPassword");

        when(passwordEncoder.matches(
                "wrongPassword",
                "hashedPassword"
        )).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals(
                "Invalid credentials",
                exception.getMessage()
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void login_shouldNotGenerateJwtWhenUserLookupFails() {
        LoginRequest request = mock(LoginRequest.class);

        when(request.getEmail()).thenReturn("missing@example.com");

        when(userService.getUserByEmail("missing@example.com"))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(request)
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }
}
