package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Project;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findAllByUserId(UUID userId);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    Project findByIdAndUserId(UUID id, UUID userId);

    List<Project> findAllByNameAndUserId(String name, UUID userId);
}
