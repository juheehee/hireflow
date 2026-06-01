package com.hireflow.hireflow.domain.coverletter.controller;

import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterDraftRequestDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterRequestDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterResponseDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterScoreResponseDto;
import com.hireflow.hireflow.domain.coverletter.service.CoverLetterService;
import com.hireflow.hireflow.global.common.ApiResponse;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/cover-letters")
@RequiredArgsConstructor
@Tag(name = "CoverLetter", description = "자소서 API")
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @Operation(summary = "자소서 작성")
    @PostMapping
    public ResponseEntity<ApiResponse<CoverLetterResponseDto>> create(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoverLetterRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.create(userDetails.getId(), applicationId, dto)));
    }

    @Operation(summary = "자소서 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CoverLetterResponseDto>>> getList(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.getList(userDetails.getId(), applicationId)));
    }

    @Operation(summary = "자소서 수정")
    @PatchMapping("/{coverLetterId}")
    public ResponseEntity<ApiResponse<CoverLetterResponseDto>> update(
            @PathVariable Long applicationId,
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoverLetterRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.update(userDetails.getId(), applicationId, coverLetterId, dto)));
    }

    @Operation(summary = "자소서 삭제")
    @DeleteMapping("/{coverLetterId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long applicationId,
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        coverLetterService.delete(userDetails.getId(), applicationId, coverLetterId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "AI 자소서 채점")
    @PostMapping("/{coverLetterId}/score")
    public ResponseEntity<ApiResponse<CoverLetterScoreResponseDto>> score(
            @PathVariable Long applicationId,
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.score(userDetails.getId(), applicationId, coverLetterId)));
    }

    @Operation(summary = "AI 자소서 초안 생성")
    @PostMapping("/draft")
    public ResponseEntity<ApiResponse<CoverLetterResponseDto>> draft(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CoverLetterDraftRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.draft(userDetails.getId(), applicationId, dto)));
    }

    @Operation(summary = "AI 자소서 첨삭")
    @PostMapping("/{coverLetterId}/feedback")
    public ResponseEntity<ApiResponse<CoverLetterResponseDto>> feedback(
            @PathVariable Long applicationId,
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                coverLetterService.feedback(userDetails.getId(), applicationId, coverLetterId)));
    }
}
