package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
