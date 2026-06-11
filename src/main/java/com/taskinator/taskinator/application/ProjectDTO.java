package com.taskinator.taskinator.application;

import com.taskinator.taskinator.domain.entity.Project;
import com.taskinator.taskinator.domain.entity.Task;
import com.taskinator.taskinator.domain.entity.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProjectDTO {
    private Long id;

    private String name;

    private String description;

    private final LocalDateTime createdAt;

    private User user;

    private List<Task> tasks = new ArrayList<>();

    public ProjectDTO(Project project) {
        this.id = project.getId();
        this.name = project.getName();
        this.description = project.getDescription();
        this.createdAt = project.getCreatedAt();
        this.user = project.getUser();
        this.tasks = project.getTasks();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public  LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
