package com.hireflow.hireflow.domain.notification;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.user.ResumeParseStatus;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Transactional
public class NotificationSchedulerTest {

    @Autowired
    NotificationScheduler notificationScheduler;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JobPostingRepository jobPostingRepository;

    @Autowired
    ApplicationRepository applicationRepository;

    @Test
    void N1_확인용() {
        // 1. 테스트용 유저 생성
        User user = User.builder()
                .email("n1test@test.com")
                .passwordHash("dummy")
                .name("N1테스트")
                .techStack("Java,Spring")
                .createdAt(LocalDateTime.now())
                .resumeParseStatus(ResumeParseStatus.NONE)
                .build();
        userRepository.save(user);

        // 2. 마감일이 오늘 +3일인 공고 생성
        JobPosting jobPosting = JobPosting.builder()
                .title("N1 테스트 공고")
                .company("테스트회사")
                .location("서울")
                .description("설명")
                .techStackTags("Java")
                .deadline(LocalDate.now().plusDays(3))
                .sourceUrl("https://test.com/" + System.currentTimeMillis())
                .source("MANUAL")
                .build();
        jobPostingRepository.save(jobPosting);

        //3. 그 공고에 대한 지원 생성
        Application application = Application.builder()
                .status("APPLIED")
                .appliedAt(LocalDate.now())
                .user(user)
                .jobPosting(jobPosting)
                .build();
        applicationRepository.save(application);

        // 4. 스케줄러 직접 호출 -> 콘솔 SQL 로그 확인
        notificationScheduler.sendDeadlineReminders();
    }

    @Test
    void N1_확인용_면접() {
        User user = User.builder()
                .email("n1test2@test.com")
                .passwordHash("dummy")
                .name("N1테스트2")
                .techStack("Java,Spring")
                .createdAt(LocalDateTime.now())
                .resumeParseStatus(ResumeParseStatus.NONE)
                .build();
        userRepository.save(user);

        JobPosting jobPosting = JobPosting.builder()
                .title("N1 면접 테스트 공고")
                .company("테스트회사")
                .location("서울")
                .description("설명")
                .techStackTags("Java")
                .deadline(LocalDate.now().plusMonths(1)) // 마감은 상관없으니 여유있게
                .sourceUrl("https://test.com/" + System.currentTimeMillis())
                .source("MANUAL")
                .build();
        jobPostingRepository.save(jobPosting);

        Application application = Application.builder()
                .status("DOC_PASS")
                .appliedAt(LocalDate.now())
                .interviewDate(LocalDate.now().plusDays(1)) // 면접 D-1 조건
                .user(user)
                .jobPosting(jobPosting)
                .build();
        applicationRepository.save(application);

        notificationScheduler.sendInterviewReminders();
    }
}
