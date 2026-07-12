package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.task.TaskDTO;
import com.taskinator.taskinator.application.task.TaskService;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.web.dto.CreateTaskRequest;
import com.taskinator.taskinator.web.dto.UpdateTaskRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasksInProject(
        @PathVariable UUID projectId,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(taskService.findAllByProject(projectId, currentUser.id()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(taskService.findTaskById(projectId, taskId, currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<TaskDTO> createTask(
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateTaskRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(taskService.createTask(projectId, request, currentUser.id()));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @Valid @RequestBody UpdateTaskRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(taskService.updateTask(projectId, taskId, request, currentUser.id()));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(
        @PathVariable UUID projectId,
        @PathVariable UUID taskId,
        @CurrentUser CurrentUserDetails currentUser) {
        taskService.deleteTask(projectId, taskId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}