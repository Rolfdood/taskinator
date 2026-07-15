package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.AddMemberRequest;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataSet(value = Datasets.EMPTY, cleanAfter = true)
class ProjectMemberControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";
    private static final String SEEDED_PROJECT_ID = "b2c3d4e5-0000-4000-8000-000000000001";
    private static final String MEMBER_ROLE_ID = "e5f6a7b8-0000-4000-8000-000000000001";

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void listMembers_shouldReturnEmptyList_whenNoMembers() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void listMembers_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void listMembers_shouldReturnNotFound_whenUserIsNotMember() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .header(AUTHORIZATION, "Bearer " + otherUserToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void addMember_shouldAddMember() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        registerAndGetAccessToken(
            "new.member@example.com", "MemberPass123!", "New", "Member");

        AddMemberRequest request = new AddMemberRequest("new.member@example.com", UUID.fromString(MEMBER_ROLE_ID));

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("new.member@example.com"))
            .andExpect(jsonPath("$.roleName").value("Member"));
    }

    @Test
    void addMember_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        AddMemberRequest request = new AddMemberRequest("someone@example.com", UUID.randomUUID());

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void addMember_shouldReturnNotFound_whenEmailDoesNotExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        AddMemberRequest request = new AddMemberRequest("nonexistent@example.com",
            UUID.fromString(MEMBER_ROLE_ID));

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void addMember_shouldReturnBadRequest_whenAddingSelf() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        AddMemberRequest request = new AddMemberRequest(EXISTING_EMAIL, UUID.fromString(MEMBER_ROLE_ID));

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void removeMember_shouldReturnNotFound_whenMemberDoesNotExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID + "/members/00000000-0000-0000-0000-000000000000")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
