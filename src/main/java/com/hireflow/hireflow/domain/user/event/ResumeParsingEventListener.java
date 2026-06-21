package com.hireflow.hireflow.domain.user.event;

import com.hireflow.hireflow.domain.notification.Notification;
import com.hireflow.hireflow.domain.notification.repository.NotificationRepository;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.infra.ai.OpenAiService;
import com.hireflow.hireflow.infra.pdf.PdfParserService;
import com.hireflow.hireflow.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResumeParsingEventListener {

    private final S3Service s3Service;
    private final PdfParserService pdfService;
    private final UserRepository userRepository;
    private final OpenAiService openAiService;
    private final NotificationRepository notificationRepository;

    @Async
    @EventListener
    public void handleResumeUploaded(ResumeUploadedEvent event) {

        try {
            // 1. S3에서 PDF 다운로드
            byte[] pdfBytes = s3Service.download(event.getResumeUrl());

            // 2. PDF에서 텍스트 추출
            String resumeText = pdfService.extractText(pdfBytes);

            // 3. OpenAI로 기술스택 파싱
            String parsedTechStack = openAiService.parseResumeToTechStack(resumeText);
            log.info("파싱 결과: {}",parsedTechStack);

            // 4. User 업데이트
            User user = userRepository.findById(event.getUserId())
                    .orElseThrow(() -> new RuntimeException("유저 없음"));
            user.completeResumeParsing(parsedTechStack);
            userRepository.save(user);

            // 5. Notification 저장
            Notification notification = Notification.builder()
                    .user(user)
                    .type("PARSE_COMPLETED")
                    .message("이력서 파싱이 완료되었습니다. 추출된 기술스택: " + parsedTechStack)
                    .sentAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);

        } catch (Exception e) {
            log.error("이력서 파싱 실패: {}", e.getMessage());

            userRepository.findById(event.getUserId()).ifPresent(user -> {
                user.failResumeParsing();
                userRepository.save(user);
            });
        }
    }
}
