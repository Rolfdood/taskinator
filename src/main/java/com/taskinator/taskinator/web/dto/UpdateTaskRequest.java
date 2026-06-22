package com.taskinator.taskinator.web.dto;

import com.taskinator.taskinator.domain.TaskStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class UpdateTaskRequest {
    @NotNull
    @NotEmpty
    String title;

    String description;

    @NotNull
    @NotEmpty
    TaskStatus status;

    LocalDate dueDate;

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

    public  void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

}
