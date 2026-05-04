package com.hireflow.hireflow.domain.user.controller;

import com.hireflow.hireflow.domain.user.dto.TechStackConfirmRequestDto;
import com.hireflow.hireflow.domain.user.dto.UserResponseDto;
import com.hireflow.hireflow.domain.user.service.UserService;
import com.hireflow.hireflow.global.common.ApiResponse;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getMe(userDetails.getId())));
    }

    // 이력서 업로드
    @PostMapping(value = "/me/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart MultipartFile file) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.uploadResume(userDetails.getId(), file)));
    }

    // 파싱 상태 조회
    @GetMapping("/me/resume/parse-status")
    public ResponseEntity<ApiResponse<String>> getResumeParseStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getResumeParseStatus(userDetails.getId())));
    }

    // 파싱된 기술 스택 확정
    @PatchMapping("/me/tech-stack/confirm")
    public ResponseEntity<ApiResponse<UserResponseDto>> confirmTechStack(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TechStackConfirmRequestDto dto) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.confirmTechStack(userDetails.getId(), dto)));
    }
}
