package com.hireflow.hireflow.domain.jobposting.service;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingRequestDto;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingResponseDto;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.match.Match;
import com.hireflow.hireflow.domain.match.MatchRepository;
import com.hireflow.hireflow.global.exception.NotFoundException;
import com.hireflow.hireflow.infra.crawler.JobSource;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final MatchRepository matchRepository;

    // 공고 목록 캐싱, keyword, tech 조합마다 다른 캐시 키 생성
    @Cacheable(value = "jobPostings", key = "'list_' + (#keyword ?: 'ALL') + '_' + (#tech ?: 'ALL')")
    public List<JobPostingResponseDto> getJobPostings(String keyword, String tech) {
        return jobPostingRepository
                .searchByKeywordAndTech(keyword, tech)
                .stream()
                .map(JobPostingResponseDto::new)
                .collect(Collectors.toList());
    }

    public JobPostingResponseDto getJobPosting(Long id) {
        JobPosting jp = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("공고를 찾을 수 없습니다."));
        return new JobPostingResponseDto(jp);
    }

    // 추천 캐싱 + 기술스택 매칭 로직
    @Cacheable(value = "recommendations", key = "'rec_' + #userId")
    public List<JobPostingResponseDto> getRecommendations(Long userId) {
        List<Match> matches = matchRepository.findByUserIdOrderByScoreDesc(userId);

        if (matches.isEmpty()) {
            return Collections.emptyList();
        }

        return matches.stream()
                .limit(3)
                .map(m -> new JobPostingResponseDto(m.getJobPosting(), m.getScore(), m.getRecommendationReason()))
                .toList();
    }

    // 공고 등록 시 캐시 무효화
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
    public JobPostingResponseDto createJobPosting(JobPostingRequestDto dto) {
        JobPosting jp = JobPosting.builder()
                .title(dto.getTitle())
                .company(dto.getCompany())
                .location(dto.getLocation())
                .description(dto.getDescription())
                .techStackTags(dto.getTechStackTags() != null ? dto.getTechStackTags() : "")
                .deadline(dto.getDeadline())
                .sourceUrl(dto.getSourceUrl() != null ? dto.getSourceUrl() : "")
                .source(JobSource.MANUAL)
                .crawledAt(null)
                .build();
        return new JobPostingResponseDto(jobPostingRepository.save(jp));
    }

    // 공고 수정 시 캐시 무효화
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
    public JobPostingResponseDto updateJobPosting(Long id, JobPostingRequestDto dto) {
        JobPosting jp = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("공고를 찾을 수 없습니다."));
        jp.update(dto);
        return new JobPostingResponseDto(jp);
    }

    // 공고 삭제 시 캐시 무효화
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
    public void deleteJobPosting(Long id) {
        jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("공고를 찾을 수 없습니다."));
        jobPostingRepository.deleteById(id);
    }
}
