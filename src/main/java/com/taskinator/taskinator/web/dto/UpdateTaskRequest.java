package com.taskinator.taskinator.web.dto;

import com.taskinator.taskinator.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record UpdateTaskRequest(
    @NotNull
    @NotEmpty
    @NotBlank
    String title,

    String description,

    @NotNull
    @NotEmpty
    @NotBlank
    String status,

    LocalDateTime dueDate
) {

}
