package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.domain.ProjectPermission;
import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.entity.User;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import com.taskinator.taskinator.domain.repository.UserRepository;
import com.taskinator.taskinator.exception.NotFoundException;
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
    private final UserRepository userRepository;
    private final ProjectValidationService projectValidationService;

    static final String TASK_NOT_FOUND = "Task not found";

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
        UserRepository userRepository,
        ProjectValidationService projectValidationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<TaskDTO> findAllByProject(UUID projectId, UUID userId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.PROJECT_VIEW);
        return taskRepository.findAllByProjectId(projectId)
            .stream()
            .map(TaskDTO::new)
            .toList();
    }

    public TaskDTO findTaskById(UUID projectId, UUID taskId, UUID userId) {
        projectValidationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.PROJECT_VIEW);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));
        return new TaskDTO(task);
    }

    @Transactional
    public TaskDTO createTask(UUID projectId, CreateTaskRequest request, UUID userId) {
        return createTask(projectId, request, userId, null);
    }

    @Transactional
    public TaskDTO createTask(UUID projectId, CreateTaskRequest request, UUID userId, UUID assigneeId) {
        projectValidationService.validatePermission(projectId, userId, ProjectPermission.TASK_CREATE);

        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        Task task = new Task(
            request.title(),
            request.description(),
            resolveStatus(request.status()),
            request.dueDate() != null ? request.dueDate().toLocalDate() : null,
            project
        );

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new NotFoundException("Assigned user not found"));
            task.setAssignedTo(assignee);
        }

        taskRepository.save(task);
        return new TaskDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(UUID projectId, UUID taskId, UpdateTaskRequest request, UUID userId) {
        projectValidationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.TASK_EDIT);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(resolveStatus(request.status()));
        task.setDueDate(request.dueDate() != null ? request.dueDate().toLocalDate() : null);

        taskRepository.save(task);
        return new TaskDTO(task);
    }

    @Transactional
    public void deleteTask(UUID projectId, UUID taskId, UUID userId) {
        projectValidationService.validateTaskAccess(taskId, projectId, userId, ProjectPermission.TASK_DELETE);
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException(TASK_NOT_FOUND));
        taskRepository.delete(task);
    }

    private TaskStatus resolveStatus(String status) {
        if (status.equalsIgnoreCase("DONE")) {
            return TaskStatus.DONE;
        } else if (status.equalsIgnoreCase("IN_PROGRESS")) {
            return TaskStatus.IN_PROGRESS;
        } else {
            return TaskStatus.TODO;
        }
    }
}