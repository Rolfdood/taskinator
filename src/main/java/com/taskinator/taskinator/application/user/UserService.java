package com.taskinator.taskinator.application.user;

import com.taskinator.taskinator.common.validation.ValidationPatterns;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDTO getCurrentUserInfo() {
        UUID uuid = getCurrentUserId();

        if (userRepository.existsById(uuid)) {
            User user = userRepository.findById(uuid).get();

            return new UserDTO(user.getName(), user.getEmail());
        } else {
            throw new NoSuchElementException("User not found");
        }
    }

    @Transactional
    public UserDTO changeUserEmail(String email) {
        UUID uuid = getCurrentUserId();

        if (userRepository.existsById(uuid)) {
            User user = userRepository.findById(uuid).get();
            user.setEmail(email);
            userRepository.save(user);
            return new UserDTO(user.getName(), user.getEmail());
        } else {
            throw new NoSuchElementException("User not found");
        }
    }

    @Transactional
    public UserDTO changeUserPassword(String oldPassword, String newPassword) {
        UUID uuid = getCurrentUserId();

        User user = userRepository.findById(uuid)
            .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(
                "New password must be different from the current password");
        }

        if (!ValidationPatterns.PASSWORD.matcher(newPassword).matches()) {
            throw new IllegalArgumentException(
                "Password must be at least 8 characters and contain uppercase, lowercase, and a number"
            );
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return new UserDTO(user.getName(), user.getEmail());
    }

    private UUID getCurrentUserId() {
        throw new UnsupportedOperationException("Current user resolver not yet implemented");
    }
}
