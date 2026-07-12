package com.taskinator.taskinator.infrastructure.security;

import com.taskinator.taskinator.domain.entity.User;

import java.util.UUID;

public record CurrentUserDetails(
    UUID id,
    String email,
    String firstName,
    String middleName,
    String lastName,
    String suffix
) {
    public static CurrentUserDetails from(User user) {
        return new CurrentUserDetails(
            user.getId(),
            user.getEmail(),
            user.getName().getFirstName(),
            user.getName().getMiddleName(),
            user.getName().getLastName(),
            user.getName().getSuffix()
        );
    }
}