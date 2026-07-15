package com.taskinator.taskinator.web.controller;

import com.taskinator.taskinator.application.member.ProjectMemberDTO;
import com.taskinator.taskinator.application.member.ProjectMemberService;
import com.taskinator.taskinator.infrastructure.security.CurrentUser;
import com.taskinator.taskinator.infrastructure.security.CurrentUserDetails;
import com.taskinator.taskinator.web.dto.AddMemberRequest;
import com.taskinator.taskinator.web.dto.UpdateMemberRoleRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public ResponseEntity<List<ProjectMemberDTO>> listMembers(
        @PathVariable UUID projectId,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(projectMemberService.listMembers(projectId, currentUser.id()));
    }

    @PostMapping
    public ResponseEntity<ProjectMemberDTO> addMember(
        @PathVariable UUID projectId,
        @Valid @RequestBody AddMemberRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(projectMemberService.addMember(projectId, currentUser.id(), request.email(), request.roleId()));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<ProjectMemberDTO> updateMemberRole(
        @PathVariable UUID projectId,
        @PathVariable UUID userId,
        @Valid @RequestBody UpdateMemberRoleRequest request,
        @CurrentUser CurrentUserDetails currentUser) {
        return ResponseEntity.ok(
            projectMemberService.updateMemberRole(projectId, userId, currentUser.id(), request.roleId()));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
        @PathVariable UUID projectId,
        @PathVariable UUID userId,
        @CurrentUser CurrentUserDetails currentUser) {
        projectMemberService.removeMember(projectId, userId, currentUser.id());
        return ResponseEntity.noContent().build();
    }
}
