package com.hireflow.hireflow.domain.notification.repository;

import com.hireflow.hireflow.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 내 알림 목록 조회(최신순)
    List<Notification> findByUserIdOrderBySentAtDesc(Long userId);

    // 읽지 않은 알림 목록 조회 (전체 읽음 처리용)
    List<Notification> findByUserIdAndRead(Long userId, boolean read);
}
