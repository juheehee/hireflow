package com.hireflow.hireflow.domain.notification;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.notification.repository.NotificationRepository;
import com.hireflow.hireflow.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ApplicationRepository applicationRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    @Scheduled(cron = "0 0 9 * * *")
    public void sendDeadlineReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(3);
        // targetDate로 지원 목록 조회
        List<Application> applications = applicationRepository.findByJobPosting_DeadlineAndStatusNot(targetDate, "REJECTED");
        // 루프 돌면서 Notification 저장 + 메일 발송
        for (Application application : applications) {
            String email = application.getUser().getEmail();
            String jobTitle = application.getJobPosting().getTitle();
            LocalDate deadline = application.getJobPosting().getDeadline();

            // 메일 발송
            mailService.sendDeadlineReminder(email, jobTitle, deadline);

            // Notification 저장
            Notification notification = Notification.builder()
                    .type("DEADLINE_REMINDER")
                    .message(jobTitle + " 공고 마감일이 " + deadline + " 입니다.")
                    .isRead(false)
                    .sentAt(LocalDateTime.now())
                    .user(application.getUser())
                    .application(application)
                    .build();

            notificationRepository.save(notification);


        }
    }

    @Scheduled(cron = "0 0 9 * * *")
    public void sendInterviewReminders() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        // targetDate로 지원 목록 조회
        List<Application> applications = applicationRepository.findByInterviewDateAndStatusNot(targetDate, "REJECTED");
        // 루프 돌면서 Notification 저장 + 메일 발송
        for (Application application : applications) {
            String email = application.getUser().getEmail();
            String jobTitle = application.getJobPosting().getTitle();
            LocalDate interviewDate = application.getInterviewDate();

            // 메일 발송
            mailService.sendInterviewReminder(email, jobTitle, interviewDate);

            // Notification 저장
            Notification notification = Notification.builder()
                    .type("INTERVIEW_REMINDER")
                    .message(jobTitle + " 면접일이 " + interviewDate + " 입니다.")
                    .isRead(false)
                    .sentAt(LocalDateTime.now())
                    .user(application.getUser())
                    .application(application)
                    .build();

            notificationRepository.save(notification);


        }
    }
}
