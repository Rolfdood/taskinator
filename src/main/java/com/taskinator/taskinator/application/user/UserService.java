package com.taskinator.taskinator.application.user;

import com.taskinator.taskinator.application.auth.RefreshTokenService;
import com.taskinator.taskinator.common.validation.ValidationPatterns;
import com.taskinator.taskinator.domain.entity.Name;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.EmailAlreadyExistsException;
import com.taskinator.taskinator.exception.InvalidCredentialsException;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final RefreshTokenService refreshTokenService;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RefreshTokenService refreshTokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO getCurrentUserInfo(CurrentUserDetails user) {
        return new UserDTO(user.firstName(), user.middleName(), user.lastName(), user.suffix(), user.email());
    }

    @Transactional
    public UserDTO changeUserEmail(CurrentUserDetails currentUserDetails, String newEmail) {
        if (currentUserDetails.email().equalsIgnoreCase(newEmail)) {
            throw new IllegalArgumentException("New email must be different from your current email");
        }

        if (userRepository.existsByEmail(newEmail)) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = findUser(currentUserDetails.id());
        user.setEmail(newEmail);
        userRepository.save(user);

        return new UserDTO(user.getName(), user.getEmail());
    }

    @Transactional
    public UserDTO changeUserPassword(
        CurrentUserDetails currentUserDetails,
        String currentPassword,
        String newPassword) {

        if (!ValidationPatterns.PASSWORD.matcher(newPassword).matches()) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters and contain uppercase, lowercase, and a number"
            );
        }

        User user = findUser(currentUserDetails.id());

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        refreshTokenService.revokeAllForUser(user.getId());

        return new UserDTO(user.getName(), user.getEmail());
    }

    private User findUser (UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

}
