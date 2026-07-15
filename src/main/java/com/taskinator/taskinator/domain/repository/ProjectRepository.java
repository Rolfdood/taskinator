package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Project;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByUserId(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Project findByIdAndUserId(UUID id, UUID userId);

    List<Project> findAllByNameAndUserId(String name, UUID userId);

    @Query("SELECT p FROM Project p WHERE p.user.id = :userId OR p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId)")
    List<Project> findAllAccessibleByUserId(@Param("userId") UUID userId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p WHERE p.id = :projectId AND (p.user.id = :userId OR p.id IN (SELECT pm.project.id FROM ProjectMember pm WHERE pm.user.id = :userId))")
    boolean existsByIdAndAccessibleByUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}
