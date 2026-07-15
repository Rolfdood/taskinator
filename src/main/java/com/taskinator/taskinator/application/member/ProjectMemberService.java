package com.taskinator.taskinator.application.member;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.ProjectMember;
import com.taskinator.taskinator.domain.entity.ProjectRole;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.ProjectRoleRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRoleRepository projectRoleRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ProjectValidationService projectValidationService;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository,
        ProjectRoleRepository projectRoleRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository,
        TaskRepository taskRepository,
        ProjectValidationService projectValidationService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRoleRepository = projectRoleRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<ProjectMemberDTO> listMembers(UUID projectId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);
        return projectMemberRepository.findAllByProjectId(projectId)
            .stream()
            .map(ProjectMemberDTO::new)
            .toList();
    }

    @Transactional
    public ProjectMemberDTO addMember(UUID projectId, UUID userId, String email, UUID roleId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        User targetUser = userRepository.findByEmail(email)
            .orElseThrow(() -> new NotFoundException("User with email '" + email + "' not found."));

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found."));

        if (project.getUser().getId().equals(targetUser.getId())) {
            throw new IllegalArgumentException("The project administrator cannot be added as a member.");
        }

        if (projectMemberRepository.existsByUserIdAndProjectId(targetUser.getId(), projectId)) {
            throw new IllegalArgumentException("User is already a member of this project.");
        }

        ProjectRole role = projectRoleRepository.findByIdAndProjectId(roleId, projectId)
            .orElseThrow(() -> new NotFoundException("Role not found in this project."));

        ProjectMember member = new ProjectMember(targetUser, project, role);
        projectMemberRepository.save(member);

        return new ProjectMemberDTO(member);
    }

    @Transactional
    public ProjectMemberDTO updateMemberRole(UUID projectId, UUID targetUserId, UUID userId, UUID roleId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(targetUserId, projectId)
            .orElseThrow(() -> new NotFoundException("Member not found in this project."));

        ProjectRole role = projectRoleRepository.findByIdAndProjectId(roleId, projectId)
            .orElseThrow(() -> new NotFoundException("Role not found in this project."));

        member.setRole(role);
        projectMemberRepository.save(member);

        return new ProjectMemberDTO(member);
    }

    @Transactional
    public void removeMember(UUID projectId, UUID targetUserId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.MEMBER_MANAGE);

        if (!projectMemberRepository.existsByUserIdAndProjectId(targetUserId, projectId)) {
            throw new NotFoundException("Member not found in this project.");
        }

        taskRepository.unassignTasksByProjectIdAndUserId(projectId, targetUserId);
        projectMemberRepository.deleteByUserIdAndProjectId(targetUserId, projectId);
    }
}
