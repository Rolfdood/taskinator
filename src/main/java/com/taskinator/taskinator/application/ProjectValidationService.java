package com.taskinator.taskinator.application;

import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.entity.ProjectMember;
import com.taskinator.taskinator.domain.repository.ProjectMemberRepository;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProjectValidationService {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectValidationService(ProjectRepository projectRepository, TaskRepository taskRepository,
        ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public void validateTaskBelongsToProject(UUID taskId, UUID projectId) {
        boolean exists = taskRepository.existsByIdAndProjectId(taskId, projectId);
        if (!exists) {
            throw new NotFoundException("Task not found in project.");
        }
    }

    public void validateProjectAccess(UUID projectId, UUID userId) {
        boolean exists = projectRepository.existsByIdAndAccessibleByUserId(projectId, userId);
        if (!exists) {
            throw new NotFoundException("Project not found.");
        }
    }

    public void validatePermission(UUID projectId, UUID userId, ProjectPermission permission) {
        if (projectRepository.existsByIdAndUserId(projectId, userId)) {
            return;
        }

        ProjectMember member = projectMemberRepository.findByUserIdAndProjectId(userId, projectId)
            .orElse(null);

        if (member != null && member.getRole().getPermissions().contains(permission)) {
            return;
        }

        throw new NotFoundException("Project not found.");
    }

    public void validateTaskAccess(UUID taskId, UUID projectId, UUID userId, ProjectPermission permission) {
        validatePermission(projectId, userId, permission);
        validateTaskBelongsToProject(taskId, projectId);
    }

    public void validateProjectBelongsToUser(UUID projectId, UUID userId) {
        validateProjectAccess(projectId, userId);
    }

    public void validateTaskOwnership(UUID taskId, UUID projectId, UUID userId) {
        validateTaskAccess(taskId, projectId, userId, ProjectPermission.PROJECT_VIEW);
    }
}
