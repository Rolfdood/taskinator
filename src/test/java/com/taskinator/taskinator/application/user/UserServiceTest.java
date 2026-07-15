package com.taskinator.taskinator.application.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taskinator.taskinator.application.auth.RefreshTokenService;
import com.taskinator.taskinator.domain.entity.Name;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.EmailAlreadyExistsException;
import com.taskinator.taskinator.exception.InvalidCredentialsException;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getCurrentUserInfo_returnsUserDTO() {
        CurrentUserDetails user = new CurrentUserDetails(
            UUID.randomUUID(), "user@example.com", "John", "M", "Doe", "Jr"
        );

        UserDTO result = userService.getCurrentUserInfo(user);

        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        assertEquals("John", result.getName().getFirstName());
        assertEquals("M", result.getName().getMiddleName());
        assertEquals("Doe", result.getName().getLastName());
        assertEquals("Jr", result.getName().getSuffix());
    }

    @Test
    void changeUserEmail_changesSuccessfully_returnsUserDTO() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "old@example.com", "John", null, "Doe", null
        );
        User user = mock(User.class);
        when(user.getName()).thenReturn(new Name("John", "Doe"));
        when(user.getEmail()).thenReturn("new@example.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserDTO result = userService.changeUserEmail(currentUser, "new@example.com");

        verify(userRepository).save(user);
        verify(user).setEmail("new@example.com");
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void changeUserEmail_sameEmail_throwsIllegalArgumentException() {
        CurrentUserDetails currentUser = new CurrentUserDetails(
            UUID.randomUUID(), "same@example.com", "John", null, "Doe", null
        );

        assertThrows(IllegalArgumentException.class,
            () -> userService.changeUserEmail(currentUser, "SAME@example.com"));
    }

    @Test
    void changeUserEmail_emailAlreadyExists_throwsEmailAlreadyExistsException() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "old@example.com", "John", null, "Doe", null
        );

        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
            () -> userService.changeUserEmail(currentUser, "taken@example.com"));
    }

    @Test
    void changeUserEmail_userNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "old@example.com", "John", null, "Doe", null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.changeUserEmail(currentUser, "new@example.com"));
    }

    @Test
    void changeUserPassword_changesSuccessfully_returnsUserDTO_andRevokesTokens() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "user@example.com", "John", null, "Doe", null
        );
        User user = mock(User.class);
        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn(new Name("John", "Doe"));
        when(user.getEmail()).thenReturn("user@example.com");
        when(user.getPasswordHash()).thenReturn("old-hash");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("CorrectOld1", "old-hash")).thenReturn(true);
        when(passwordEncoder.matches("NewPass1", "old-hash")).thenReturn(false);
        when(passwordEncoder.encode("NewPass1")).thenReturn("new-hash");

        UserDTO result = userService.changeUserPassword(currentUser, "CorrectOld1", "NewPass1");

        verify(userRepository).save(user);
        verify(user).setPasswordHash("new-hash");
        verify(refreshTokenService).revokeAllForUser(userId);
        assertEquals("user@example.com", result.getEmail());
    }

    @Test
    void changeUserPassword_invalidPattern_throwsIllegalArgumentException() {
        CurrentUserDetails currentUser = new CurrentUserDetails(
            UUID.randomUUID(), "user@example.com", "John", null, "Doe", null
        );

        assertThrows(IllegalArgumentException.class,
            () -> userService.changeUserPassword(currentUser, "OldPass1", "short"));
    }

    @Test
    void changeUserPassword_wrongCurrentPassword_throwsInvalidCredentialsException() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "user@example.com", "John", null, "Doe", null
        );
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("hashed");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1", "hashed")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> userService.changeUserPassword(currentUser, "WrongPass1", "NewPass1"));

        verify(refreshTokenService, never()).revokeAllForUser(userId);
    }

    @Test
    void changeUserPassword_sameAsCurrentPassword_throwsIllegalArgumentException() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "user@example.com", "John", null, "Doe", null
        );
        User user = mock(User.class);
        when(user.getPasswordHash()).thenReturn("hashed");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("SamePass1", "hashed")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> userService.changeUserPassword(currentUser, "SamePass1", "SamePass1"));
    }

    @Test
    void changeUserPassword_userNotFound_throwsNotFoundException() {
        UUID userId = UUID.randomUUID();
        CurrentUserDetails currentUser = new CurrentUserDetails(
            userId, "user@example.com", "John", null, "Doe", null
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
            () -> userService.changeUserPassword(currentUser, "OldPass1", "NewPass1"));
    }
}
