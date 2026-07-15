package com.taskinator.taskinator.application.task;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taskinator.taskinator.domain.TaskStatus;
import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class TaskDTO {
    UUID id;

    String title;

    String description;

    TaskStatus status;

    LocalDate dueDate;

    LocalDateTime createdAt;

    Project project;

    UUID assignedToId;

    String assignedToName;

    String assignedToEmail;

    public TaskDTO(Task task) {
        this.id = task.getId();
        this.title = task.getTitle();
        this.description = task.getDescription();
        this.status = task.getStatus();
        this.dueDate = task.getDueDate();
        this.createdAt = task.getCreatedAt();
        this.project = task.getProject();
        if (task.getAssignedTo() != null) {
            this.assignedToId = task.getAssignedTo().getId();
            this.assignedToName = task.getAssignedTo().getName().getFullName();
            this.assignedToEmail = task.getAssignedTo().getEmail();
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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

    @JsonIgnore
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public UUID getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(UUID assignedToId) {
        this.assignedToId = assignedToId;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }
}
