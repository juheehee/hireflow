package com.hireflow.hireflow.domain.notification.repository;

import com.hireflow.hireflow.domain.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
