package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
    @NotBlank
    String name,

    String description
) {

}
