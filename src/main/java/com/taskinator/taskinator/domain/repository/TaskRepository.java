package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Task;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    boolean existsByIdAndProjectId(Long id, Long projectId);

    List<Task> findAllByProjectId(Long projectId);
}
