package com.hireflow.hireflow.domain.user.controller;

import com.hireflow.hireflow.domain.user.dto.TechStackConfirmRequestDto;
import com.hireflow.hireflow.domain.user.dto.UserResponseDto;
import com.hireflow.hireflow.domain.user.service.UserService;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "사용자 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<UserResponseDto>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(userService.getMe(userDetails.getId())));
    }

    @Operation(summary = "이력서 PDF 업로드", description = "이력서 PDF를 S3에 업로드하고 AI 파싱을 비동기로 시작합니다. 파싱 상태는 PENDING으로 변경됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 성공 — 파싱 시작됨"),
            @ApiResponse(responseCode = "400", description = "파일 형식 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping(value = "/me/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<UserResponseDto>> uploadResume(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart MultipartFile file) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(userService.uploadResume(userDetails.getId(), file)));
    }

    @Operation(summary = "이력서 파싱 상태 조회", description = "AI 이력서 파싱 진행 상태를 반환합니다. NONE / PENDING / COMPLETED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/me/resume/parse-status")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<String>> getResumeParseStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(userService.getResumeParseStatus(userDetails.getId())));
    }

    @Operation(summary = "기술스택 확정", description = "AI가 파싱한 기술스택을 확인하고 최종 확정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기술스택 확정 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/me/tech-stack/confirm")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<UserResponseDto>> confirmTechStack(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody TechStackConfirmRequestDto dto) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(userService.confirmTechStack(userDetails.getId(), dto)));
    }
}