package com.hireflow.hireflow.domain.coverletter.repository;

import com.hireflow.hireflow.domain.coverletter.CoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {

    List<CoverLetter> findByApplicationId(Long applicationId);
}
