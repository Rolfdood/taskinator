package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsByIdAndUserId(Long id, Long userId);

    Project findByIdAndUserId(Long projectId, Long userId);

    List<Project> findAllByUserId(Long userId);
}
