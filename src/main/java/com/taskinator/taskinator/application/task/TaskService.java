package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.application.ProjectValidationService;
import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    /**
     * TODO:
     *  - Revise methods for proper parameters and return types. (DTOs, records, and primitive types)
     *  - Implement Service Layer Validation
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

        List<Task> tasks = taskRepository.findAllByProjectId(projectId);

        List<TaskDTO> taskDTOList = new ArrayList<>();

        for(Task task : tasks) {
            taskDTOList.add(new TaskDTO(task));
        }

        return taskDTOList;
    }

    public TaskDTO findTaskById(Long taskId, Long projectId) {
        projectValidationService.validateTaskBelongsToProject(taskId, projectId);

        return new TaskDTO(taskRepository.findById(taskId).get());
    }

    public List<TaskDTO> findAllByProjectId(Long projectId) {
        if(projectRepository.findById(projectId).isPresent()) {
            List<Task> tasks = taskRepository.findAllByProjectId(projectId);

            List<TaskDTO> taskDTOList = new ArrayList<>();

            for(Task task : tasks) {
                taskDTOList.add(new TaskDTO(task));
            }

            return taskDTOList;
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public TaskDTO createTask(Long projectId, String title, String description, LocalDate dueDate, TaskStatus status) {
        if(projectRepository.findById(projectId).isPresent()) {
            Project project = projectRepository.findById(projectId).get();

            Task newTask = new Task(title, description, status, dueDate, project);

            project.getTasks().add(newTask);

            taskRepository.save(newTask);
            projectRepository.save(project);

            return new TaskDTO(newTask);
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public TaskDTO updateTask(Long projectId, Long taskId, String title, String description, LocalDate dueDate,
        TaskStatus status) {
        if(projectRepository.findById(projectId).isPresent()) {
            projectValidationService.validateTaskBelongsToProject(taskId, projectId);

            Task task = taskRepository.findById(taskId).get();
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setStatus(status);

            taskRepository.save(task);
            return new TaskDTO(task);
        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        if(projectRepository.findById(projectId).isPresent()) {
            Project project = projectRepository.findById(projectId).get();

            projectValidationService.validateTaskBelongsToProject(taskId, projectId);

            Task task = taskRepository.findById(taskId).get();

            project.getTasks().remove(task);
            taskRepository.delete(task);
        } else {
            throw new NotFoundException("Project not found");
        }
    }

}
