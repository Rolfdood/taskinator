package com.taskinator.taskinator.domain.repository;

import com.taskinator.taskinator.domain.entity.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

}
