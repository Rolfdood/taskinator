package com.taskinator.taskinator.application.task;

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

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    public List<TaskDTO> findAll() {

        List<Task> tasks = taskRepository.findAll();

        List<TaskDTO> taskDTOList = new ArrayList<>();

        for(Task task : tasks) {
            taskDTOList.add(new TaskDTO(task));
        }

        return taskDTOList;
    }

    public TaskDTO findTaskById(Long taskId) {
        if(taskRepository.findById(taskId).isPresent()) {
            return new TaskDTO(taskRepository.findById(taskId).get());
        } else {
            throw new NotFoundException("Task not found");
        }
    }

    public List<TaskDTO> findAllByProjectId(Long projectId) {

        if(projectRepository.findById(projectId).isPresent()) {
            Project project = projectRepository.findById(projectId).get();

            List<Task> tasks = project.getTasks();

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
            Project project = projectRepository.findById(projectId).get();

            if(taskRepository.findById(taskId).isPresent()) {
                Task task = taskRepository.findById(taskId).get();
                if(project.getTasks().contains(task)) {
                    task.setTitle(title);
                    task.setDescription(description);
                    task.setDueDate(dueDate);
                    task.setStatus(status);

                    taskRepository.save(task);
                    return new TaskDTO(task);

                } else {
                    throw new NotFoundException("Task not found in Project.");
                }

            } else {
                throw new NotFoundException("Task not found.");
            }

        } else {
            throw new NotFoundException("Project not found");
        }
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        if(projectRepository.findById(projectId).isPresent()) {
            Project project = projectRepository.findById(projectId).get();

            if(taskRepository.findById(taskId).isPresent()) {
                Task task = taskRepository.findById(taskId).get();

                if(project.getTasks().contains(task)) {
                    project.getTasks().remove(task);
                    taskRepository.delete(task);
                }
            }
        }
    }

}
