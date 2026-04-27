package com.hireflow.hireflow.domain.jobposting.service;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingRequestDto;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingResponseDto;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.infra.crawler.JobSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    public List<JobPostingResponseDto> getJobPostings(String keyword, String tech) {
        return jobPostingRepository
                .searchByKeywordAndTech(keyword, tech)
                .stream()
                .map(JobPostingResponseDto::new)
                .collect(Collectors.toList());
    }

    public JobPostingResponseDto getJobPosting(Long id) {
        JobPosting jp = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        return new JobPostingResponseDto(jp);
    }

    @Transactional
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

    @Transactional
    public JobPostingResponseDto updateJobPosting(Long id, JobPostingRequestDto dto) {
        JobPosting jp = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        jp.update(dto);
        return new JobPostingResponseDto(jp);
    }

    @Transactional
    public void deleteJobPosting(Long id) {
        jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공고를 찾을 수 없습니다."));
        jobPostingRepository.deleteById(id);
    }

    public List<JobPostingResponseDto> getRecommendations() {
        // Day 6에 Redis 캐싱 + 기술스택 매칭 로직 추가 예정
        return jobPostingRepository.findAll()
                .stream()
                .limit(3)
                .map(JobPostingResponseDto::new)
                .collect(Collectors.toList());
    }
}
