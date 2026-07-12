package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.AbstractDBUnitTest;
import com.taskinator.taskinator.web.dto.auth.LoginRequest;
import com.taskinator.taskinator.web.dto.auth.RegisterRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    void refresh_shouldReturnUnauthorized_whenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
            .andExpect(status().isUnauthorized());
    }
}