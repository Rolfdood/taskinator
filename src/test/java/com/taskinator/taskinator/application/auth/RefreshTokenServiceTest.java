package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.RefreshToken;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.RefreshTokenRepository;
import com.taskinator.taskinator.exception.TokenRefreshException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    private static final long REFRESH_TOKEN_EXPIRATION_MS = 604_800_000L;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private RefreshTokenService refreshTokenService;

    @BeforeEach
    void setUp() {
        refreshTokenService = new RefreshTokenService(refreshTokenRepository, REFRESH_TOKEN_EXPIRATION_MS);
    }

    private static void setId(User user, UUID id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }

    private static String sha256Base64(String raw) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return Base64.getEncoder().encodeToString(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void issueToken_savesHashedTokenAndReturnsRawToken() throws Exception {
        User user = new User("test@example.com", "hashed-password", null);

        String rawToken = refreshTokenService.issueToken(user);

        assertNotNull(rawToken);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken saved = captor.getValue();

        assertSame(user, saved.getUser());
        assertEquals(sha256Base64(rawToken), saved.getTokenHash());
        assertFalse(saved.isRevoked());
        assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
        assertTrue(saved.getExpiresAt().isBefore(
            LocalDateTime.now().plusMinutes(REFRESH_TOKEN_EXPIRATION_MS/60000).plusSeconds(5)));
    }

    @Test
    void issueToken_generatesDifferentRawTokenOnEachCall() {
        User user = new User("test@example.com", "hashed-password", null);

        String first = refreshTokenService.issueToken(user);
        String second = refreshTokenService.issueToken(user);

        assertNotEquals(first, second);
    }

    @Test
    void rotateToken_withValidToken_revokesExistingAndIssuesNewToken() {
        User user = new User("test@example.com", "hashed-password", null);
        RefreshToken existing = new RefreshToken(user, "old-hash", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));

        RefreshTokenService.RotationResult result = refreshTokenService.rotateToken("old-raw-token");

        assertSame(user, result.user());
        assertNotNull(result.newRawToken());

        assertTrue(existing.isRevoked());
        assertNotNull(existing.getRevokedAt());
        assertNotNull(existing.getReplacedByTokenHash());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();

        assertSame(existing, saved.get(0));
        RefreshToken newToken = saved.get(1);
        assertSame(user, newToken.getUser());
        assertFalse(newToken.isRevoked());
        assertEquals(existing.getReplacedByTokenHash(), newToken.getTokenHash());

        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void rotateToken_withUnrecognizedToken_throwsTokenRefreshException() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
            () -> refreshTokenService.rotateToken("unknown-token"));
        assertTrue(exception.getMessage().contains("not recognized"));

        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void rotateToken_withAlreadyRevokedToken_detectsReuseAndRevokesAllSessions() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User("test@example.com", "hashed-password", null);
        setId(user, userId);

        RefreshToken existing = new RefreshToken(user, "old-hash", LocalDateTime.now().plusDays(1));
        existing.setRevoked(true);
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
            () -> refreshTokenService.rotateToken("stolen-token"));
        assertTrue(exception.getMessage().contains("reuse detected"));

        verify(refreshTokenRepository).revokeAllByUserId(userId);
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void rotateToken_withExpiredToken_throwsTokenRefreshException() {
        User user = new User("test@example.com", "hashed-password", null);
        RefreshToken existing = new RefreshToken(user, "old-hash", LocalDateTime.now().minusMinutes(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));

        TokenRefreshException exception = assertThrows(TokenRefreshException.class,
            () -> refreshTokenService.rotateToken("expired-token"));
        assertTrue(exception.getMessage().contains("expired"));

        verify(refreshTokenRepository, never()).save(any());
        verify(refreshTokenRepository, never()).revokeAllByUserId(any());
    }

    @Test
    void revokeToken_withExistingToken_marksItRevokedAndSaves() {
        User user = new User("test@example.com", "hashed-password", null);
        RefreshToken existing = new RefreshToken(user, "hash", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(existing));

        refreshTokenService.revokeToken("raw-token");

        assertTrue(existing.isRevoked());
        assertNotNull(existing.getRevokedAt());
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void revokeToken_withUnknownToken_doesNothing() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        refreshTokenService.revokeToken("unknown-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void revokeAllForUser_delegatesToRepository() {
        UUID userId = UUID.randomUUID();

        refreshTokenService.revokeAllForUser(userId);

        verify(refreshTokenRepository).revokeAllByUserId(userId);
    }
}