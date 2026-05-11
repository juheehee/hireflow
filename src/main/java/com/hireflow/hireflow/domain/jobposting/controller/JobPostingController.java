package com.hireflow.hireflow.domain.jobposting.controller;

import com.hireflow.hireflow.domain.jobposting.dto.JobPostingRequestDto;
import com.hireflow.hireflow.domain.jobposting.dto.JobPostingResponseDto;
import com.hireflow.hireflow.domain.jobposting.service.JobPostingService;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "JobPosting", description = "채용공고 API")
@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @Operation(summary = "공고 목록 조회", description = "키워드/기술스택으로 공고를 검색합니다. 결과는 Redis에 10분간 캐싱됩니다. 비로그인도 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<List<JobPostingResponseDto>>> getJobPostings(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tech) {
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(jobPostingService.getJobPostings(keyword, tech)));
    }

    @Operation(summary = "공고 Top 3 추천", description = "로그인한 사용자의 기술스택과 매칭되는 공고를 최대 3개 추천합니다. 결과는 Redis에 캐싱됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "추천 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/recommendations")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<List<JobPostingResponseDto>>> getRecommendations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(jobPostingService.getRecommendations(userDetails.getId())));
    }

    @Operation(summary = "공고 상세 조회", description = "공고 ID로 상세 정보를 조회합니다. 비로그인도 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<JobPostingResponseDto>> getJobPosting(
            @PathVariable Long id) {
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(jobPostingService.getJobPosting(id)));
    }

    @Operation(summary = "공고 직접 등록", description = "크롤링 외에 공고를 수동으로 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "입력값 오류")
    })
    @PostMapping
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<JobPostingResponseDto>> createJobPosting(
            @RequestBody JobPostingRequestDto dto) {
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(jobPostingService.createJobPosting(dto)));
    }

    @Operation(summary = "공고 수정", description = "공고 정보를 수정합니다. id/source/sourceUrl/crawledAt은 수정 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<JobPostingResponseDto>> updateJobPosting(
            @PathVariable Long id, @RequestBody JobPostingRequestDto dto) {
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(jobPostingService.updateJobPosting(id, dto)));
    }

    @Operation(summary = "공고 삭제", description = "공고를 삭제합니다. 관련 캐시도 함께 삭제됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "공고를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<Void>> deleteJobPosting(
            @PathVariable Long id) {
        jobPostingService.deleteJobPosting(id);
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(null));
    }
}