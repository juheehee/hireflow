package com.hireflow.hireflow.domain.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hireflow.hireflow.domain.notification.Notification;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDto {

    private Long id;
    private String type;
    private String message;
    private boolean read;
    private LocalDateTime sentAt;
    private Long applicationId; // nullable

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.type = notification.getType();
        this.message = notification.getMessage();
        this.read = notification.isRead();
        this.sentAt = notification.getSentAt();
        this.applicationId = notification.getApplication() != null
                ? notification.getApplication().getId() : null;
    }
}
