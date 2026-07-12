package com.taskinator.taskinator.web.dto.auth;

public record AuthResponse(
    String accessToken,
    long expiresIn
) {}