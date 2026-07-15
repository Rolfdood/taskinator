package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.ProjectRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRoleRepository extends JpaRepository<ProjectRole, UUID> {

    List<ProjectRole> findAllByProjectId(UUID projectId);

    Optional<ProjectRole> findByIdAndProjectId(UUID id, UUID projectId);

    boolean existsByProjectIdAndName(UUID projectId, String name);

    void deleteByIdAndProjectId(UUID id, UUID projectId);
}
