package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.role.ProjectRoleDTO;
import com.taskinator.taskinator.application.role.ProjectRoleService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.web.dto.CreateRoleRequest;
import com.taskinator.taskinator.web.dto.UpdateRoleRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/roles")
public class ProjectRoleController {

    private final ProjectRoleService projectRoleService;

    public ProjectRoleController(ProjectRoleService projectRoleService) {
        this.projectRoleService = projectRoleService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectRoleDTO>> listRoles(
        @PathVariable UUID projectId,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(projectRoleService.listRoles(projectId, currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<ProjectRoleDTO> createRole(
        @PathVariable UUID projectId,
        @Valid @RequestBody CreateRoleRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        Set<ProjectPermission> permissions = request.permissions().stream()
            .map(ProjectPermission::valueOf)
            .collect(Collectors.toSet());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectRoleService.createRole(projectId, currentUser.id(), request.name(), permissions));
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ProjectRoleDTO> updateRole(
        @PathVariable UUID projectId,
        @PathVariable UUID roleId,
        @Valid @RequestBody UpdateRoleRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        Set<ProjectPermission> permissions = request.permissions().stream()
            .map(ProjectPermission::valueOf)
            .collect(Collectors.toSet());
        return ResponseEntity.ok(
            projectRoleService.updateRole(projectId, roleId, currentUser.id(), request.name(), permissions));
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(
        @PathVariable UUID projectId,
        @PathVariable UUID roleId,
        @CurrentUser CurrentUserDetails currentUser) {
        projectRoleService.deleteRole(projectId, roleId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
