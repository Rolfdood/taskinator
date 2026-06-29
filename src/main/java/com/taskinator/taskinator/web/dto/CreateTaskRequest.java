package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateTaskRequest (
    @NotBlank
    String title,

    String description,

    @NotNull
    String status,

    LocalDateTime dueDate

){

}
