package com.cellc.userservice.repository;

import com.cellc.userservice.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    List<UserRole> findAllByUserId(Long userId);

    List<UserRole> findByUserId(Long aLong);
}
