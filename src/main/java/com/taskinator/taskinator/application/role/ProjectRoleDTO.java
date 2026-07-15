package com.taskinator.taskinator.application.role;

import com.taskinator.taskinator.domain.entity.ProjectRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectRoleDTO(
    UUID id,
    String name,
    List<String> permissions,
    LocalDateTime createdAt
) {
    public ProjectRoleDTO(ProjectRole role) {
        this(
            role.getId(),
            role.getName(),
            role.getPermissions().stream()
                .map(Enum::name)
                .toList(),
            role.getCreatedAt()
        );
    }
}
