package dev.kristof.spendly.auth;

import dev.kristof.spendly.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString(
                    "0123456789abcdef0123456789abcdef"
                            .getBytes(StandardCharsets.UTF_8)
            );

    private static final long EXPIRATION = 60_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    private User mockUser(String email) {
        User user = mock(User.class);
        when(user.getEmail()).thenReturn(email);
        return user;
    }

    @Test
    void generateJwt_shouldReturnValidToken() {
        User user = mockUser("test@example.com");

        String token = jwtService.generateJwt(user);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void extractEmail_shouldReturnCorrectEmail() {
        User user = mockUser("test@example.com");

        String token = jwtService.generateJwt(user);

        assertEquals(
                "test@example.com",
                jwtService.extractEmail(token)
        );
    }

    @Test
    void generateJwt_shouldSetCorrectExpiration() {
        User user = mockUser("test@example.com");

        String token = jwtService.generateJwt(user);

        Claims claims = Jwts.parser()
                .verifyWith(
                        Keys.hmacShaKeyFor(
                                Decoders.BASE64.decode(SECRET)
                        )
                )
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());

        long actualExpiration =
                claims.getExpiration().getTime()
                        - claims.getIssuedAt().getTime();

        assertEquals(EXPIRATION, actualExpiration);
    }

    @Test
    void extractEmail_shouldRejectExpiredToken() {
        JwtService expiredJwtService =
                new JwtService(SECRET, -10_000L);

        User user = mockUser("test@example.com");

        String token = expiredJwtService.generateJwt(user);

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    @Test
    void extractEmail_shouldRejectTokenWithDifferentSecret() {
        String differentSecret = Base64.getEncoder()
                .encodeToString(
                        "abcdef0123456789abcdef0123456789"
                                .getBytes(StandardCharsets.UTF_8)
                );

        JwtService otherJwtService =
                new JwtService(differentSecret, EXPIRATION);

        User user = mockUser("test@example.com");

        String token = jwtService.generateJwt(user);

        assertThrows(
                JwtException.class,
                () -> otherJwtService.extractEmail(token)
        );
    }

    @Test
    void extractEmail_shouldRejectMalformedToken() {
        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail("not-a-valid-jwt")
        );
    }

    @Test
    void constructor_shouldRejectWeakSecret() {
        String weakSecret = Base64.getEncoder()
                .encodeToString(
                        "short-secret"
                                .getBytes(StandardCharsets.UTF_8)
                );

        assertThrows(
                WeakKeyException.class,
                () -> new JwtService(weakSecret, EXPIRATION)
        );
    }
}
