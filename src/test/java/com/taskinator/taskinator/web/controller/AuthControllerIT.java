package com.taskinator.taskinator.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.auth.LoginRequest;
import com.taskinator.taskinator.web.dto.auth.RegisterRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@DataSet(value = Datasets.EMPTY, cleanAfter = true)
class AuthControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";

    @Test
    void register_shouldCreateUserAndReturnTokens_whenEmailIsNew() throws Exception {
        RegisterRequest request = new RegisterRequest(
            "new.user@example.com", "SecurePass123!", "John", "Smith");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.expiresIn").isNumber())
            .andExpect(header().string("Set-Cookie", containsString("refreshToken=")));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void register_shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
            EXISTING_EMAIL, "AnyPassword123!", "Jane", "Doe");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.title").value("Email Already Registered"));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void login_shouldReturnTokens_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void login_shouldReturnUnauthorized_whenPasswordIsWrong() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, "WrongPassword!");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.title").value("Authentication Failed"));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void login_shouldSetSecureHttpOnlyCookie() throws Exception {
        LoginRequest request = new LoginRequest(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(header().string("Set-Cookie", containsString("refreshToken=")))
            .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
            .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")))
            .andExpect(header().string("Set-Cookie", containsString("Path=/api/v1/auth")));
    }

    @Test
    void refresh_shouldReturnUnauthorized_whenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void refresh_shouldReturnNewAccessTokenAndRotateCookie() throws Exception {
        String refreshToken = loginAndGetRefreshToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(header().string("Set-Cookie", containsString("refreshToken=")));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void refresh_shouldRevokeAllSessions_whenReused() throws Exception {
        String refreshToken = loginAndGetRefreshToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void logout_shouldClearCookieAndRevokeToken() throws Exception {
        String refreshToken = loginAndGetRefreshToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(post("/api/v1/auth/logout")
                .cookie(new Cookie("refreshToken", refreshToken)))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Set-Cookie", containsString("refreshToken=;")))
            .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .cookie(new Cookie("refreshToken", refreshToken)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void accessWithInvalidJwt_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                .header(AUTHORIZATION, "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }

    private String loginAndGetRefreshToken(String email, String password) throws Exception {
        LoginRequest request = new LoginRequest(email, password);

        var result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andReturn();

        return extractRefreshTokenCookie(result);
    }
}