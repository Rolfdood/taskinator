package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.infrastructure.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_withExistingEmail_returnsUserPrincipal() {
        User user = new User("test@example.com", "hashed-password", null);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("test@example.com");

        assertInstanceOf(UserPrincipal.class, result);
        assertSame(user, ((UserPrincipal) result).getUser());
        assertEquals("test@example.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_withUnknownEmail_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
        assertTrue(exception.getMessage().contains("missing@example.com"));
    }

    @Test
    void loadUserById_withExistingId_returnsUserPrincipal() {
        UUID id = UUID.randomUUID();
        User user = new User("test@example.com", "hashed-password", null);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserById(id);

        assertInstanceOf(UserPrincipal.class, result);
        assertSame(user, ((UserPrincipal) result).getUser());
    }

    @Test
    void loadUserById_withUnknownId_throwsUsernameNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserById(id));
        assertTrue(exception.getMessage().contains(id.toString()));
    }
}