package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}
