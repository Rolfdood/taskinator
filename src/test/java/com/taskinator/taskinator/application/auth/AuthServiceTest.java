package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.EmailAlreadyExistsException;
import com.taskinator.taskinator.exception.InvalidCredentialsException;
import com.taskinator.taskinator.exception.TokenRefreshException;
import com.taskinator.taskinator.infrastructure.security.UserPrincipal;
import com.taskinator.taskinator.web.dto.auth.LoginRequest;
import com.taskinator.taskinator.web.dto.auth.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long ACCESS_TOKEN_EXPIRATION_MS = 900_000L;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtService,
            refreshTokenService,
            ACCESS_TOKEN_EXPIRATION_MS
        );
    }

    @Test
    void register_withNewEmail_savesUserAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe");

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-password");
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("access-token");
        when(refreshTokenService.issueToken(any(User.class))).thenReturn("refresh-token");

        AuthResult result = authService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPasswordHash());

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(ACCESS_TOKEN_EXPIRATION_MS / 1000, result.expiresIn());

        verify(refreshTokenService).issueToken(savedUser);
        verify(jwtService).generateAccessToken(argThat(principal -> principal.getUser() == savedUser));
    }

    @Test
    void register_withExistingEmail_throwsAndDoesNotSaveOrIssueTokens() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "John", "Doe");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, jwtService, refreshTokenService);
    }

    @Test
    void login_withValidCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = new User("test@example.com", "hashed-password", null);
        UserPrincipal principal = new UserPrincipal(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
        when(refreshTokenService.issueToken(user)).thenReturn("refresh-token");

        AuthResult result = authService.login(request);

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
            ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertEquals("test@example.com", tokenCaptor.getValue().getPrincipal());
        assertEquals("password123", tokenCaptor.getValue().getCredentials());

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals(ACCESS_TOKEN_EXPIRATION_MS / 1000, result.expiresIn());
    }

    @Test
    void login_withBadCredentials_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void login_whenPrincipalIsNotUserPrincipal_throwsInvalidCredentialsException() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new Object());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));

        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    void refresh_withValidToken_rotatesAndReturnsNewTokens() {
        String rawRefreshToken = "old-raw-token";
        User user = new User("test@example.com", "hashed-password", null);
        RefreshTokenService.RotationResult rotationResult =
            new RefreshTokenService.RotationResult(user, "new-raw-token");

        when(refreshTokenService.rotateToken(rawRefreshToken)).thenReturn(rotationResult);
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-access-token");

        AuthResult result = authService.refresh(rawRefreshToken);

        assertEquals("new-access-token", result.accessToken());
        assertEquals("new-raw-token", result.refreshToken());
        assertEquals(ACCESS_TOKEN_EXPIRATION_MS / 1000, result.expiresIn());

        verify(jwtService).generateAccessToken(argThat(principal -> principal.getUser() == user));
        verify(refreshTokenService, never()).issueToken(any());
        verifyNoInteractions(userRepository, passwordEncoder, authenticationManager);
    }

    @Test
    void refresh_whenRotationFails_propagatesTokenRefreshException() {
        String rawRefreshToken = "reused-raw-token";
        when(refreshTokenService.rotateToken(rawRefreshToken))
            .thenThrow(new TokenRefreshException("Refresh token reuse detected — all sessions revoked"));

        assertThrows(TokenRefreshException.class, () -> authService.refresh(rawRefreshToken));

        verifyNoInteractions(jwtService);
    }

    @Test
    void logout_delegatesToRefreshTokenService() {
        String rawRefreshToken = "raw-token";

        authService.logout(rawRefreshToken);

        verify(refreshTokenService).revokeToken(rawRefreshToken);
        verifyNoInteractions(jwtService, userRepository, passwordEncoder, authenticationManager);
    }
}