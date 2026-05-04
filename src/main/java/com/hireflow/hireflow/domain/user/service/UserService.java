package com.hireflow.hireflow.domain.user.service;

import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.dto.UserResponseDto;
import com.hireflow.hireflow.domain.user.event.ResumeUploadedEvent;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.exception.NotFoundException;
import com.hireflow.hireflow.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    // 내 프로필 조회
    public UserResponseDto getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        return new UserResponseDto(user);
    }

    // 이력서 업로드
    @Transactional
    public UserResponseDto uploadResume(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        // S3 업로드 → URL 반환
        String resumeUrl = s3Service.upload(file);

        // User 엔티티에 URL + 상태 업데이트
        user.uploadResume(resumeUrl);

        // 이벤트 발행 — 비동기로 파싱 시작
        eventPublisher.publishEvent(new ResumeUploadedEvent(userId, resumeUrl));

        return new UserResponseDto(user);
    }

    // 파싱 상태 조회
    public String getResumeParseStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));
        return user.getResumeParseStatus();
    }
}
