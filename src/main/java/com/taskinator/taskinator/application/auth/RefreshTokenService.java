package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.RefreshToken;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.RefreshTokenRepository;
import com.taskinator.taskinator.exception.TokenRefreshException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final long refreshTokenExpirationMs;
    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenService(
        RefreshTokenRepository refreshTokenRepository,
        @Value("${application.jwt.refresh-token-expiration-ms}") long refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    @Transactional
    public String issueToken(User user) {
        String rawToken = generateRawToken();
        RefreshToken token = new RefreshToken(user, hash(rawToken), expiry());
        refreshTokenRepository.save(token);
        return rawToken;
    }

    @Transactional
    public RotationResult rotateToken(String rawToken) {
        String hash = hash(rawToken);

        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
            .orElseThrow(() -> new TokenRefreshException("Refresh token not recognized"));

        if (existing.isRevoked()) {
            refreshTokenRepository.revokeAllByUserId(existing.getUser().getId());
            throw new TokenRefreshException("Refresh token reuse detected — all sessions revoked");
        }

        if (existing.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenRefreshException("Refresh token expired");
        }

        String newRawToken = generateRawToken();
        String newHash = hash(newRawToken);

        existing.setRevoked(true);
        existing.setRevokedAt(LocalDateTime.now());
        existing.setReplacedByTokenHash(newHash);
        refreshTokenRepository.save(existing);

        RefreshToken newToken = new RefreshToken(existing.getUser(), newHash, expiry());
        refreshTokenRepository.save(newToken);

        return new RotationResult(existing.getUser(), newRawToken);
    }

    @Transactional
    public void revokeToken(String rawToken) {
        refreshTokenRepository.findByTokenHash(hash(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private LocalDateTime expiry() {
        return LocalDateTime.now().plus(Duration.ofMillis(refreshTokenExpirationMs));
    }

    private String generateRawToken() {
        byte[] randomBytes = new byte[64];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public record RotationResult(User user, String newRawToken) {}
}