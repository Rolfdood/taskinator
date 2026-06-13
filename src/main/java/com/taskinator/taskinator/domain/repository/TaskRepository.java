package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

}
