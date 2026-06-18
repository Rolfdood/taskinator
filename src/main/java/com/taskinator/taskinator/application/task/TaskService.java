package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    /**
     * TODO: Add ownership validation once the currently logged-in user resolver is implemented.
     *  - Before any operation, verify the projectId belongs to the currently logged-in user via
     *    ProjectValidationService.validateProjectBelongsToUser(projectId, getCurrentUserId()).
     *  - This prevents users from accessing or mutating tasks in projects they don't own.
     */

    private final TaskRepository taskRepository;

    private final ProjectRepository projectRepository;

    private final ProjectValidationService projectValidationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
        ProjectValidationService projectValidationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.projectValidationService = projectValidationService;
    }

    public List<TaskDTO> findAllTasksInProject(Long projectId) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        List<Task> tasks = taskRepository.findAllByProjectId(projectId);

        List<TaskDTO> taskDTOList = new ArrayList<>();
        for (Task task : tasks) {
            taskDTOList.add(new TaskDTO(task));
        }

        return taskDTOList;
    }

    public TaskDTO findTaskById(Long projectId, Long taskId) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        projectValidationService.validateTaskBelongsToProject(taskId, projectId);

        return new TaskDTO(taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found")));
    }

    @Transactional
    public TaskDTO createTask(Long projectId, CreateTaskRequest createTaskRequest) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        Task newTask = new Task(
            createTaskRequest.getTitle(),
            createTaskRequest.getDescription(),
            createTaskRequest.getStatus(),
            createTaskRequest.getDueDate(),
            project
        );

        project.getTasks().add(newTask);
        taskRepository.save(newTask);
        projectRepository.save(project);

        return new TaskDTO(newTask);
    }

    @Transactional
    public TaskDTO updateTask(Long projectId, Long taskId, UpdateTaskRequest updateTaskRequest) {
        projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        projectValidationService.validateTaskBelongsToProject(taskId, projectId);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));

        task.setTitle(updateTaskRequest.getTitle());
        task.setDescription(updateTaskRequest.getDescription());
        task.setDueDate(updateTaskRequest.getDueDate());
        task.setStatus(updateTaskRequest.getStatus());

        taskRepository.save(task);

        return new TaskDTO(task);
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new NotFoundException("Project not found"));

        projectValidationService.validateTaskBelongsToProject(taskId, projectId);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new NotFoundException("Task not found"));

        project.getTasks().remove(task);
        taskRepository.delete(task);
    }

}