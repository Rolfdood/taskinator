package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateRoleRequest(
    @NotBlank
    String name,

    @NotNull
    @Size(min = 1)
    List<String> permissions
) {}
