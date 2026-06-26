package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProjectRequest (
    @NotNull
    @NotBlank
    String name,
    String description
){

}
