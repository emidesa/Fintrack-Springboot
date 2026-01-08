package com.fintrack.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fintrack.api.entity.User;
import com.fintrack.api.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByIsActive(Boolean isActive);
    List<User> findByRoleAndIsActive(Role role, Boolean isActive);
}