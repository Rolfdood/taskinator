package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataSet(value = Datasets.EMPTY, cleanAfter = true)
class ProjectControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";
    private static final String SEEDED_PROJECT_ID = "b2c3d4e5-0000-4000-8000-000000000001";

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void findAllProjects_shouldReturnProjects_whenProjectsExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Project 1"));
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void findProjectsByName_shouldReturnMatchingProjects_whenNameMatches() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/search")
                .param("name", "Project 1")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void findProjectsByName_shouldReturnNotFound_whenNoMatch() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/search")
                .param("name", "Nonexistent Project")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.USERS, cleanBefore = true)
    void createProject_shouldCreateProject_whenRequestIsValid() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        CreateProjectRequest request = new CreateProjectRequest("Project 2", "PHP project");

        mockMvc.perform(post("/api/v1/projects")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Project 2"))
            .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void updateProject_shouldUpdateProject_whenUserOwnsProject() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        UpdateProjectRequest request = new UpdateProjectRequest("Project 1.0", "Project 1 revision");

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Project 1.0"));
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void updateProject_shouldReturnNotFound_whenUserDoesNotOwnProject() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");
        UpdateProjectRequest request = new UpdateProjectRequest("Hijacked Name", "Malicious edit");

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID)
                .header(AUTHORIZATION, "Bearer " + otherUserToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void deleteProject_shouldDeleteProject_whenUserOwnsProject() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID)
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    @DataSet(value = Datasets.PROJECTS, cleanBefore = true)
    void deleteProject_shouldReturnNotFound_whenUserDoesNotOwnProject() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID)
                .header(AUTHORIZATION, "Bearer " + otherUserToken))
            .andExpect(status().isNotFound());
    }
}