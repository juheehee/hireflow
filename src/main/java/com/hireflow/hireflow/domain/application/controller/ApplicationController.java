package com.hireflow.hireflow.domain.application.controller;

import com.hireflow.hireflow.domain.application.dto.ApplicationRequestDto;
import com.hireflow.hireflow.domain.application.dto.ApplicationResponseDto;
import com.hireflow.hireflow.domain.application.service.ApplicationService;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Application", description = "지원 현황 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(summary = "지원 등록", description = "채용공고에 지원을 등록합니다. 동일 공고 중복 지원 불가.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지원 등록 성공"),
            @ApiResponse(responseCode = "400", description = "중복 지원 또는 공고 없음"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<ApplicationResponseDto>> crateApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ApplicationRequestDto dto) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.createApplication(userDetails.getId(), dto)));
    }

    @Operation(summary = "내 지원 목록 조회", description = "로그인한 사용자의 전체 지원 목록을 최신순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<List<ApplicationResponseDto>>> getApplications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.getApplications(userDetails.getId())));
    }

    @Operation(summary = "지원 상세 조회", description = "지원 ID로 상세 정보를 조회합니다. 본인 지원 내역만 조회 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "본인 지원 내역 아님"),
            @ApiResponse(responseCode = "404", description = "지원 내역 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<ApplicationResponseDto>> getApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.getApplication(userDetails.getId(), id)));
    }

    @Operation(summary = "지원 상태 변경", description = "지원 상태를 변경합니다. APPLIED / DOC_PASS / INTERVIEW_1 / INTERVIEW_2 / FINAL_PASS / REJECTED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 상태값 또는 본인 지원 아님"),
            @ApiResponse(responseCode = "404", description = "지원 내역 없음")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<ApplicationResponseDto>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.updateStatus(userDetails.getId(), id, body.get("status"))));
    }

    @Operation(summary = "면접 날짜 등록", description = "면접 날짜를 등록합니다. D-1에 이메일 알림이 발송됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "면접 날짜 등록 성공"),
            @ApiResponse(responseCode = "404", description = "지원 내역 없음")
    })
    @PatchMapping("/{id}/interview-date")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<ApplicationResponseDto>> updateInterviewDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LocalDate interviewDate = LocalDate.parse(body.get("interviewDate"));
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.updateInterviewDate(userDetails.getId(), id, interviewDate)));
    }

    @Operation(summary = "메모 수정", description = "지원 내역에 메모를 작성하거나 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메모 수정 성공"),
            @ApiResponse(responseCode = "404", description = "지원 내역 없음")
    })
    @PatchMapping("/{id}/memo")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<ApplicationResponseDto>> updateMemo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        applicationService.updateMemo(userDetails.getId(), id, body.get("memo"))));
    }

    @Operation(summary = "지원 삭제", description = "지원 내역을 삭제합니다. 본인 지원 내역만 삭제 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "지원 내역 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        applicationService.deleteApplication(userDetails.getId(), id);
        return ResponseEntity.noContent().build();
    }
}