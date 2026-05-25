package com.hireflow.hireflow.domain.match;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MatchRepository extends JpaRepository<Match, Long> {

    List<Match> findByUserIdOrderByScoreDesc(Long userId);

    Optional<Match> findByUserIdAndJobPostingId(Long userId, Long jobPostingId);

    void deleteByUserId(Long userId);
}
