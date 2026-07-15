package com.taskinator.taskinator.application.project;

import com.taskinator.taskinator.application.member.ProjectMemberDTO;
import com.taskinator.taskinator.application.role.ProjectRoleDTO;
import com.taskinator.taskinator.application.task.TaskDTO;
import com.taskinator.taskinator.domain.entity.Project;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProjectDTO(
    UUID id,
    String name,
    String description,
    LocalDateTime createdAt,
    UUID userId,
    List<TaskDTO> tasks,
    List<ProjectRoleDTO> roles,
    List<ProjectMemberDTO> members
) {
    public ProjectDTO(Project project) {
        this(
            project.getId(),
            project.getName(),
            project.getDescription(),
            project.getCreatedAt(),
            project.getUser().getId(),
            project.getTasks().stream().map(TaskDTO::new).toList(),
            project.getRoles().stream().map(ProjectRoleDTO::new).toList(),
            project.getMembers().stream().map(ProjectMemberDTO::new).toList()
        );
    }
}
