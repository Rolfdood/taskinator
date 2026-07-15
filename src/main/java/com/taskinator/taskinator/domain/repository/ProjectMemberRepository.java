package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.ProjectMember;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, UUID> {

    List<ProjectMember> findAllByProjectId(UUID projectId);

    Optional<ProjectMember> findByUserIdAndProjectId(UUID userId, UUID projectId);

    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);

    void deleteByUserIdAndProjectId(UUID userId, UUID projectId);

    boolean existsByRoleId(UUID roleId);
}
