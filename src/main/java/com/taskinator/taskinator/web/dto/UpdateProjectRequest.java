package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateProjectRequest(
    @NotNull
    UUID projectId,
    @NotNull
    @NotEmpty
    String name,
    String description
) {

}
