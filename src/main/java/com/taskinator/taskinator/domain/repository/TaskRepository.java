package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Task;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    boolean existsByIdAndProjectId(UUID id, UUID projectId);

    List<Task> findAllByProjectId(UUID projectId);
}
