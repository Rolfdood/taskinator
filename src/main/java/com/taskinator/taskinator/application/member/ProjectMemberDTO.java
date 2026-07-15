package com.taskinator.taskinator.application.member;

import com.taskinator.taskinator.domain.entity.ProjectMember;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProjectMemberDTO(
    UUID userId,
    String email,
    String firstName,
    String lastName,
    UUID roleId,
    String roleName,
    LocalDateTime joinedAt
) {
    public ProjectMemberDTO(ProjectMember member) {
        this(
            member.getUser().getId(),
            member.getUser().getEmail(),
            member.getUser().getName().getFirstName(),
            member.getUser().getName().getLastName(),
            member.getRole().getId(),
            member.getRole().getName(),
            member.getJoinedAt()
        );
    }
}
