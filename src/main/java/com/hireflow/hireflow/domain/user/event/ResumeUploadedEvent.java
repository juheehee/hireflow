package com.hireflow.hireflow.domain.user.event;

import lombok.Getter;

@Getter
public class ResumeUploadedEvent {

    private final Long userId;
    private final String resumeUrl;

    public ResumeUploadedEvent(Long userId, String resumeUrl) {
        this.userId = userId;
        this.resumeUrl = resumeUrl;
    }
}
