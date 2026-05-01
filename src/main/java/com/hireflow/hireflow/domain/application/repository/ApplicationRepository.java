package com.hireflow.hireflow.domain.application.repository;

import com.hireflow.hireflow.domain.application.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 내 지원 목록 (최신순)
    List<Application> findByUserIdOrderByAppliedAtDesc(Long userId);

    // 같은 공고에 중복 지원 체크
    boolean existsByUserIdAndJobPostingId(Long userId, Long jobPostingId);
}
