package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.project.ProjectDTO;
import com.taskinator.taskinator.application.project.ProjectService;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> findAllProjects(
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(projectService.findAllProjects(currentUser.id()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectDTO>> findProjectsByName(
        @RequestParam String name,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(projectService.findProjectsByName(name, currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(
        @CurrentUser CurrentUserDetails currentUser,
        @Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectService.createProject(currentUser.id(), request));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDTO> updateProject(
        @PathVariable UUID projectId,
        @CurrentUser CurrentUserDetails currentUser,
        @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, currentUser.id(), request));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(
        @PathVariable UUID projectId,
        @CurrentUser CurrentUserDetails currentUser) {
        projectService.deleteProject(projectId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}