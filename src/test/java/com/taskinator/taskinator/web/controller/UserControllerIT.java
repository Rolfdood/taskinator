package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.ChangeEmailRequest;
import com.taskinator.taskinator.web.dto.ChangePasswordRequest;
import org.junit.jupiter.api.Test;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataSet(value = Datasets.EMPTY, cleanAfter = true)
class UserControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void getCurrentUserInfo_shouldReturnUser_whenAuthenticated() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/me")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(EXISTING_EMAIL))
            .andExpect(jsonPath("$.name.firstName").value("Jane"))
            .andExpect(jsonPath("$.name.lastName").value("Doe"));
    }

    @Test
    void getCurrentUserInfo_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void changeEmail_shouldChangeEmail_whenEmailIsNew() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        ChangeEmailRequest request = new ChangeEmailRequest("new.email@example.com");

        mockMvc.perform(post("/api/v1/me/email")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new.email@example.com"));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void changeEmail_shouldReturnBadRequest_whenSameEmailDifferentCase() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        ChangeEmailRequest request = new ChangeEmailRequest("EXISTING.USER@EXAMPLE.COM");

        mockMvc.perform(post("/api/v1/me/email")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void changeEmail_shouldReturnConflict_whenEmailAlreadyTaken() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        registerAndGetAccessToken("taken@example.com", "OtherPass123!", "Other", "User");
        ChangeEmailRequest request = new ChangeEmailRequest("taken@example.com");

        mockMvc.perform(post("/api/v1/me/email")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict());
    }

    @Test
    void changeEmail_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest("new.email@example.com");

        mockMvc.perform(post("/api/v1/me/email")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void changePassword_shouldChangePassword_whenCurrentPasswordIsCorrect() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        ChangePasswordRequest request = new ChangePasswordRequest(EXISTING_PASSWORD, "NewPass123!");

        mockMvc.perform(post("/api/v1/me/password")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(EXISTING_EMAIL));
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void changePassword_shouldReturnUnauthorized_whenCurrentPasswordIsWrong() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass1", "NewPass123!");

        mockMvc.perform(post("/api/v1/me/password")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void changePassword_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("OldPass1", "NewPass123!");

        mockMvc.perform(post("/api/v1/me/password")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
