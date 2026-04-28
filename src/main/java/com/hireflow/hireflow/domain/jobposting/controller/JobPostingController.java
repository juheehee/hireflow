package com.hireflow.hireflow.domain.jobposting.controller;

import com.hireflow.hireflow.domain.jobposting.dto.JobPostingRequestDto;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingResponseDto;
import com.hireflow.hireflow.domain.jobposting.service.JobPostingService;
import com.hireflow.hireflow.global.common.ApiResponse;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobPostingResponseDto>>> getJobPostings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tech) {
        return ResponseEntity.ok(ApiResponse.success(jobPostingService.getJobPostings(keyword, tech)));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<JobPostingResponseDto>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(jobPostingService.getRecommendations(userDetails.getId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponseDto>> getJobPosting(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jobPostingService.getJobPosting(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<JobPostingResponseDto>> createJobPosting(
            @RequestBody JobPostingRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(jobPostingService.createJobPosting(dto)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<JobPostingResponseDto>> updateJobPosting(
            @PathVariable Long id, @RequestBody JobPostingRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(jobPostingService.updateJobPosting(id, dto)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobPosting(@PathVariable Long id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
