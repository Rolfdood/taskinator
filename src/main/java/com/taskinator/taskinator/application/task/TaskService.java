package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.web.dto.CreateTaskRequest;
import com.taskinator.taskinator.web.dto.UpdateTaskRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final ProjectValidationService projectValidationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
        ProjectValidationService projectValidationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<TaskDTO> findAllByProject(UUID projectId, UUID userId) {
        projectValidationService.validateProjectBelongsToUser(projectId, userId);
        return taskRepository.findAllByProjectId(projectId)
            .stream()
            .map(TaskDTO::new)
            .toList();
    }

    public TaskDTO findTaskById(UUID projectId, UUID taskId, UUID userId) {
        projectValidationService.validateTaskOwnership(taskId, projectId, userId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        return new TaskDTO(task);
    }

    @Transactional
    public TaskDTO createTask(UUID projectId, CreateTaskRequest request, UUID userId) {
        projectValidationService.validateProjectBelongsToUser(projectId, userId);
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        Task task = new Task(
            request.title(),
            request.description(),
            resolveStatus(request.status()),
            request.dueDate() != null ? request.dueDate().toLocalDate() : null,
            project
        );

        taskRepository.save(task);
        return new TaskDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request, UUID userId) {
        projectValidationService.validateTaskOwnership(taskId, projectId, userId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(resolveStatus(request.status()));
        task.setDueDate(request.dueDate() != null ? request.dueDate().toLocalDate() : null);

        taskRepository.save(task);
        return new TaskDTO(task);
    }

    @Transactional
    public void deleteTask(UUID projectId, UUID taskId, UUID userId) {
        projectValidationService.validateTaskOwnership(taskId, projectId, userId);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    private TaskStatus resolveStatus (String status) {
        if (status.equalsIgnoreCase("DONE")) {
            return TaskStatus.DONE;
        } else if (status.equalsIgnoreCase("IN_PROGRESS")) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.TODO;
        }
    }
}