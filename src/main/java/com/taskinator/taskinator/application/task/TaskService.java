package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.application.exception.NotFoundException;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.repository.ProjectRepository;
import com.taskinator.taskinator.domain.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

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
}
