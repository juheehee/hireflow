package com.hireflow.hireflow.domain.notification.service;

import com.hireflow.hireflow.domain.notification.Notification;
import com.hireflow.hireflow.domain.notification.dto.NotificationResponseDto;
import com.hireflow.hireflow.domain.notification.repository.NotificationRepository;
import com.hireflow.hireflow.global.exception.BadRequestException;
import com.hireflow.hireflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    // NotificationRepository 주입
    private final NotificationRepository notificationRepository;

    // getNotifications(Long userId) 알림 목록 반환
    public List<NotificationResponseDto> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderBySentAtDesc(userId)
                .stream().map(NotificationResponseDto::new).collect(Collectors.toList());
    }
    // markAsRead(Long userId, Long notificationId) 단건 읽음 처리
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("알림을 찾을 수 없습니다."));

        if (!notification.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 알림만 읽음 처리할 수 있습니다.");
        }
        notification.markAsRead();
    }

    // markAllAsRead(Long userId) 전체 읽음 처리
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndRead(userId, false);

        // 루프 돌면서 하나씩 markAsRead() 호출
        for (Notification notification : notifications) {
            notification.markAsRead();
        }

    }

}
