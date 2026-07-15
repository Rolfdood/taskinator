package com.taskinator.taskinator.web.controller;

import com.github.database.rider.core.api.dataset.DataSet;
import com.taskinator.taskinator.Datasets;
import com.taskinator.taskinator.web.dto.CreateTaskRequest;
import com.taskinator.taskinator.web.dto.UpdateTaskRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DataSet(value = Datasets.TASKS)
class TaskControllerIT extends AbstractDBUnitTest {

    private static final String EXISTING_EMAIL = "existing.user@example.com";
    private static final String EXISTING_PASSWORD = "Password123!";
    private static final String SEEDED_PROJECT_ID = "b2c3d4e5-0000-4000-8000-000000000001";
    private static final String SEEDED_TASK_ID = "c3d4e5f6-0000-4000-8000-000000000001";

    @Test
    void getAllTasksInProject_shouldReturnTasks_whenTasksExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Task 1"))
            .andExpect(jsonPath("$[0].status").value("TODO"));
    }

    @Test
    void getAllTasksInProject_shouldReturnNotFound_whenUserDoesNotOwnProject() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks")
                .header(AUTHORIZATION, "Bearer " + otherUserToken))
            .andExpect(status().isNotFound());
    }

    @Test
    void getAllTasksInProject_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void getTaskById_shouldReturnTask_whenTaskExists() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/" + SEEDED_TASK_ID)
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Task 1"))
            .andExpect(jsonPath("$.description").value("First task"));
    }

    @Test
    void getTaskById_shouldReturnNotFound_whenTaskDoesNotExist() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(get("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/00000000-0000-0000-0000-000000000000")
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTask_shouldCreateTask_whenRequestIsValid() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        CreateTaskRequest request = new CreateTaskRequest(
            "New Task", "Task description", "TODO",
            LocalDateTime.of(2026, 12, 31, 0, 0), null);

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks")
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Task"))
            .andExpect(jsonPath("$.description").value("Task description"))
            .andExpect(jsonPath("$.status").value("TODO"))
            .andExpect(jsonPath("$.dueDate").value("2026-12-31"));
    }

    @Test
    void createTask_shouldReturnUnauthorized_whenNotAuthenticated() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("Task", "Desc", "TODO", null, null);

        mockMvc.perform(post("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTask_shouldUpdateTask_whenUserOwnsTask() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);
        UpdateTaskRequest request = new UpdateTaskRequest(
            "Updated Task", "Updated description", "IN_PROGRESS", null, null);

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/" + SEEDED_TASK_ID)
                .header(AUTHORIZATION, "Bearer " + token)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Task"))
            .andExpect(jsonPath("$.description").value("Updated description"))
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_shouldReturnNotFound_whenUserDoesNotOwnProject() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");
        UpdateTaskRequest request = new UpdateTaskRequest(
            "Hijacked Task", "Malicious", "DONE", null, null);

        mockMvc.perform(put("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/" + SEEDED_TASK_ID)
                .header(AUTHORIZATION, "Bearer " + otherUserToken)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_shouldDeleteTask_whenUserOwnsTask() throws Exception {
        String token = loginAndGetAccessToken(EXISTING_EMAIL, EXISTING_PASSWORD);

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/" + SEEDED_TASK_ID)
                .header(AUTHORIZATION, "Bearer " + token))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_shouldReturnNotFound_whenUserDoesNotOwnProject() throws Exception {
        String otherUserToken = registerAndGetAccessToken(
            "other.user@example.com", "OtherPass123!", "Other", "User");

        mockMvc.perform(delete("/api/v1/projects/" + SEEDED_PROJECT_ID + "/tasks/" + SEEDED_TASK_ID)
                .header(AUTHORIZATION, "Bearer " + otherUserToken))
            .andExpect(status().isNotFound());
    }
}
