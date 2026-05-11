package com.hireflow.hireflow.domain.notification.controller;

import com.hireflow.hireflow.domain.notification.dto.NotificationResponseDto;
import com.hireflow.hireflow.domain.notification.service.NotificationService;
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

@Tag(name = "Notification", description = "알림 API")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "로그인한 사용자의 전체 알림 목록을 반환합니다. DEADLINE_REMINDER / INTERVIEW_REMINDER / PARSE_COMPLETED")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<List<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                com.hireflow.hireflow.global.common.ApiResponse.success(
                        notificationService.getNotifications(userDetails.getId())));
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다. 본인 알림만 처리 가능.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "읽음 처리 성공"),
            @ApiResponse(responseCode = "404", description = "알림 없음")
    })
    @PatchMapping("/{id}/read")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(userDetails.getId(), id);
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(null));
    }

    @Operation(summary = "전체 알림 읽음 처리", description = "로그인한 사용자의 모든 알림을 읽음 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 읽음 처리 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/read-all")
    public ResponseEntity<com.hireflow.hireflow.global.common.ApiResponse<Void>> markAAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(com.hireflow.hireflow.global.common.ApiResponse.success(null));
    }
}