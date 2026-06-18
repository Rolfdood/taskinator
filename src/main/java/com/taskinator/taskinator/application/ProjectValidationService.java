package com.taskinator.taskinator.application;

import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectValidationService {

    private final ProjectRepository projectRepository;

    private final TaskRepository taskRepository;

    public ProjectValidationService(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    public void validateTaskBelongsToProject(Long taskId, Long projectId) {
        boolean exists = taskRepository.existsByIdAndProjectId(taskId, projectId);
        if (!exists) {
            throw new NotFoundException("Task not found in project.");
        }
    }

    public void validateProjectBelongsToUser(Long projectId, Long userId) {
        boolean exists = projectRepository.existsByIdAndUserId(projectId, userId);
        if (!exists) {
            throw new NotFoundException("Project not found for user.");
        }
    }

    public void validateTaskOwnership(Long taskId, Long projectId, Long userId) {
        validateProjectBelongsToUser(projectId, userId);
        validateTaskBelongsToProject(taskId, projectId);
    }
}
