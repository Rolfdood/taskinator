package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.project.ProjectDTO;
import com.taskinator.taskinator.application.project.ProjectService;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.web.dto.CreateProjectRequest;
import com.taskinator.taskinator.web.dto.UpdateProjectRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> findAllProjects(@CurrentUser CurrentUserDetails currentUserDetails) {
        return ResponseEntity.ok(projectService.findAllProjects(currentUserDetails));
    }

    @GetMapping("{name}")
    public ResponseEntity<List<ProjectDTO>> findProjectsByName(@CurrentUser CurrentUserDetails currentUserDetails,
        @PathVariable String name) {
        return ResponseEntity.ok(projectService.findProjectByName(name, currentUserDetails));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(CurrentUserDetails currentUserDetails,
        @Valid CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(currentUserDetails, request));
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> updateProjectDetails(CurrentUserDetails currentUserDetails,
        @Valid UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(currentUserDetails, request));
    }

    @DeleteMapping("{id}")
    public void deleteProject(@CurrentUser CurrentUserDetails currentUserDetails, @PathVariable UUID id) {
        projectService.deleteProject(currentUserDetails, id);
    }

}
