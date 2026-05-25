package com.hireflow.hireflow.domain.user.repository;

import com.hireflow.hireflow.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // 기술스택 확정 유저만 조회
    List<User> findByTechStackIsNotNull();

    boolean existsByEmail(String email);
}
