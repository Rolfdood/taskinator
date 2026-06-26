package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest (
    @NotNull
    @NotEmpty
    @NotBlank
    String title,

    String description,

    @NotNull
    @NotEmpty
    @NotBlank
    String status,

    LocalDateTime dueDate,

    @NotNull
    @NotBlank
    UUID projectId
){

}
