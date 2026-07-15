package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.CreateRoleRequest;
import com.taskinator.taskinator.web.dto.UpdateRoleRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataSet(value = Datasets.EMPTY, cleanAfter = true)
class ProjectRoleControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";
    private static final String SEEDED_PROJECT_ID = "b2c3d4e5-0000-4000-8000-000000000001";
    private static final String MANAGER_ROLE_ID = "d4e5f6a7-0000-4000-8000-000000000001";

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void listRoles_shouldReturnRoles() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].name").value("Manager"))
            .andExpect(jsonPath("$[1].name").value("Member"));
    }

    @Test
    void listRoles_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void listRoles_shouldReturnNotFound_whenUserIsNotMember() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles")
                .header(AUTHORIZATION, "Bearer " + otherUserToken))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void createRole_shouldCreateRole() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        CreateRoleRequest request = new CreateRoleRequest("Viewer", List.of("PROJECT_VIEW"));

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Viewer"))
            .andExpect(jsonPath("$.permissions", hasSize(1)));
    }

    @Test
    void createRole_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        CreateRoleRequest request = new CreateRoleRequest("Viewer", List.of("PROJECT_VIEW"));

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void updateRole_shouldUpdateRole() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        UpdateRoleRequest request = new UpdateRoleRequest("Manager-Updated",
            List.of("PROJECT_VIEW", "TASK_CREATE"));

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles/" + MANAGER_ROLE_ID)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Manager-Updated"))
            .andExpect(jsonPath("$.permissions", hasSize(2)));
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void updateRole_shouldReturnNotFound_whenRoleDoesNotExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        UpdateRoleRequest request = new UpdateRoleRequest("Nope", List.of("PROJECT_VIEW"));

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles/00000000-0000-0000-0000-000000000000")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void deleteRole_shouldDeleteRole() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles/" + MANAGER_ROLE_ID)
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    @DataSet(value = Datasets.ROLES, cleanBefore = true)
    void deleteRole_shouldReturnNotFound_whenRoleDoesNotExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID + "/roles/00000000-0000-0000-0000-000000000000")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound());
    }
}
