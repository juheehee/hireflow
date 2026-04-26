package com.hireflow.hireflow.domain.jobposting.repository;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findBySourceUrl(String sourceUrl);

    List<JobPosting> findByTechStackTagsContaining(String tech);

    @Query("SELECT j FROM JobPosting j WHERE " +
            "(:keyword IS NULL OR j.title LIKE %:keyword% OR j.company LIKE %:keyword%) AND " +
            "(:tech IS NULL OR j.techStackTags LIKE %:tech%)")
    List<JobPosting> searchByKeywordAndTech(@Param("keyword") String keyword,
                                            @Param("tech") String tech);
}
