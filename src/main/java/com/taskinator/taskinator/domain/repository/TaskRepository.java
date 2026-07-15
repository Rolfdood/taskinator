package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Task;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByIdAndProjectId(UUID id, UUID projectId);

    List<Task> findAllByProjectId(UUID projectId);

    List<Task> findAllByProjectIdAndAssignedToId(UUID projectId, UUID userId);

    @Modifying
    @Query("UPDATE Task t SET t.assignedTo = null WHERE t.project.id = :projectId AND t.assignedTo.id = :userId")
    void unassignTasksByProjectIdAndUserId(@Param("projectId") UUID projectId, @Param("userId") UUID userId);
}