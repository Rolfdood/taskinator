package com.taskinator.taskinator.domain.entity;

import com.taskinator.taskinator.domain.TaskStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Task(String title, String description, TaskStatus status, LocalDate dueDate, Project project) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.project = project;
    }

    protected Task() {}

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Project getProject() { return project; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setProject(Project project) { this.project = project; }
}