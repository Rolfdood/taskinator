package com.taskinator.taskinator.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UpdateMemberRoleRequest(
    @NotNull
    UUID roleId
) {}
