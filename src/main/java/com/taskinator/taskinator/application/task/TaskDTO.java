package com.taskinator.taskinator.application.task;

import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDTO {
    Long id;

    String title;

    String description;

    TaskStatus status;

    LocalDate dueDate;

    LocalDateTime createdAt;

    Project project;

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();
        this.project = task.getProject();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
