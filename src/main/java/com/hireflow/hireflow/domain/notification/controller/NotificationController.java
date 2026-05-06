package com.hireflow.hireflow.domain.notification.controller;

import com.hireflow.hireflow.domain.notification.dto.NotificationResponseDto;
import com.hireflow.hireflow.domain.notification.service.NotificationService;
import com.hireflow.hireflow.global.common.ApiResponse;
import com.hireflow.hireflow.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                ApiResponse.success(notificationService.getNotifications(userDetails.getId())));

    }

    // 개인 읽음처리
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markAsRead(userDetails.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // 전체 읽음처리
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
