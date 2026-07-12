package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.infrastructure.security.UserPrincipal;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd";
    private static final String OTHER_SECRET = "fedcba9876543210fedcba9876543210fedcba9876543210fedcba98765432";
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 900_000L;

    private JwtService jwtService;
    private UserPrincipal principal;
    private UUID userId;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService(SECRET, ACCESS_TOKEN_EXPIRATION_MS);

        User user = new User("test@example.com", "hashed-password", null);
        userId = UUID.randomUUID();
        setId(user, userId);

        principal = new UserPrincipal(user);
    }

    private static void setId(User user, UUID id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }

    @Test
    void generateAccessToken_createsTokenThatIsValid() {
        String token = jwtService.generateAccessToken(principal);

        assertNotNull(token);
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void extractUserId_returnsIdEncodedInToken() {
        String token = jwtService.generateAccessToken(principal);

        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void isTokenValid_returnsFalseForMalformedToken() {
        assertFalse(jwtService.isTokenValid("this.is.not-a-jwt"));
    }

    @Test
    void isTokenValid_returnsFalseForTokenSignedWithDifferentKey() {
        JwtService otherJwtService = new JwtService(OTHER_SECRET, ACCESS_TOKEN_EXPIRATION_MS);
        String token = otherJwtService.generateAccessToken(principal);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void isTokenValid_returnsFalseForTamperedToken() {
        String token = jwtService.generateAccessToken(principal);
        String tampered = token.substring(0, token.length() - 1) + (token.endsWith("A") ? "B" : "A");

        assertFalse(jwtService.isTokenValid(tampered));
    }

    @Test
    void isTokenValid_returnsFalseForExpiredToken() {
        JwtService expiredTokenIssuer = new JwtService(SECRET, -1_000L);
        String token = expiredTokenIssuer.generateAccessToken(principal);

        assertFalse(jwtService.isTokenValid(token));
    }

    @Test
    void extractUserId_throwsForExpiredToken() {
        JwtService expiredTokenIssuer = new JwtService(SECRET, -1_000L);
        String token = expiredTokenIssuer.generateAccessToken(principal);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUserId(token));
    }
}