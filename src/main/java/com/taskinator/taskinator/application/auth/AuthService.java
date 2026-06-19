package com.taskinator.taskinator.application.auth;

import com.taskinator.taskinator.domain.entity.Name;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.infrastructure.security.UserPrincipal;
import com.taskinator.taskinator.web.dto.auth.AuthResponse;
import com.taskinator.taskinator.web.dto.auth.LoginRequest;
import com.taskinator.taskinator.web.dto.auth.RegisterRequest;
import com.taskinator.taskinator.exception.EmailAlreadyExistsException;
import com.taskinator.taskinator.exception.InvalidCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Value("${application.jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    public AuthService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            new Name(request.firstName(), request.lastName())
        );
        userRepository.save(user);

        return issueTokens(new UserPrincipal(user));
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return issueTokens((UserPrincipal) authentication.getPrincipal());
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotateToken(rawRefreshToken);
        UserPrincipal principal = new UserPrincipal(result.user());
        String newAccessToken = jwtService.generateAccessToken(principal);

        return new AuthResponse(newAccessToken, result.newRawToken(), accessTokenExpirationMs / 1000);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenService.revokeToken(rawRefreshToken);
    }

    private AuthResponse issueTokens(UserPrincipal principal) {
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.issueToken(principal.getUser());
        return new AuthResponse(accessToken, refreshToken, accessTokenExpirationMs / 1000);
    }
}