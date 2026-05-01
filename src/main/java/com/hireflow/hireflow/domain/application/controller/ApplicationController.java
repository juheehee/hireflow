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

import java.util.List;

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
}
