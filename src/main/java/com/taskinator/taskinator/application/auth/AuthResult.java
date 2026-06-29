package com.taskinator.taskinator.application.auth;

public record AuthResult(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}