package com.hireflow.hireflow.domain.application.controller;

import com.hireflow.hireflow.domain.application.dto.ApplicationRequestDto;
import com.hireflow.hireflow.domain.application.dto.ApplicationResponseDto;
import com.hireflow.hireflow.domain.application.service.ApplicationService;
import com.hireflow.hireflow.global.common.ApiResponse;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // 지원 등록
    @PostMapping
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> crateApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.createApplication(userDetails.getId(), dto)));
    }

    // 내 지원 목록
    @GetMapping
    public ResponseEntity<ApiResponse<List<ApplicationResponseDto>>> getApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.getApplications(userDetails.getId())));
    }

    // 지원 상세
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> getApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.getApplication(userDetails.getId(), id)));
    }

    // 상세 변경
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.updateStatus(
                        userDetails.getId(), id, body.get("status"))));
    }

    // 면접 날짜 등록
    @PatchMapping("/{id}/interview-date")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateInterviewDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDate interviewDate = LocalDate.parse(body.get("interviewDate"));
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.updateInterviewDate(
                        userDetails.getId(), id, interviewDate)));
    }

    // 메모 수정
    @PatchMapping("/{id}/memo")
    public ResponseEntity<ApiResponse<ApplicationResponseDto>> updateMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                ApiResponse.success(applicationService.updateMemo(
                        userDetails.getId(), id, body.get("memo"))));
    }

    // 지원 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        applicationService.deleteApplication(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }

}
