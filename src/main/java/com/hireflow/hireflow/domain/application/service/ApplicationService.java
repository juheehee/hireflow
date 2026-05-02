package com.hireflow.hireflow.domain.application.service;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.dto.ApplicationRequestDto;
import com.hireflow.hireflow.domain.application.dto.ApplicationResponseDto;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.exception.BadRequestException;
import com.hireflow.hireflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    // 유효한 상태값 목록
    private static final Set<String> VALID_STATES = Set.of(
            "APPLIED", "DOC_PASS", "INTERVIEW_1", "INTERVIEW_2", "FINAL_PASS", "REJECTED"
    );

    // 지원 등록
    @Transactional
    public ApplicationResponseDto createApplication(Long userId, ApplicationRequestDto dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        JobPosting jobPosting = jobPostingRepository.findById(dto.getJobPostingId()).orElseThrow(() -> new NotFoundException("공고를 찾을 수 없습니다."));

        // 중복 지원 체크
        if (applicationRepository.existsByUserIdAndJobPostingId(userId, dto.getJobPostingId())) {
            throw new BadRequestException("이미 지원한 공고입니다.");
        }

        Application application = Application.builder()
                .user(user)
                .jobPosting(jobPosting)
                .status("APPLIED")
                .memo(dto.getMemo())
                .appliedAt(LocalDate.now())
                .build();

        return new ApplicationResponseDto(applicationRepository.save(application));
    }

    // 내 지원 목록
    public List<ApplicationResponseDto> getApplications(Long userId) {
        return applicationRepository.findByUserIdOrderByAppliedAtDesc(userId)
                .stream().map(ApplicationResponseDto::new).collect(Collectors.toList());
    }

    // 지원 상세
    public ApplicationResponseDto getApplication(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));

        // 본인 지원 내역인지 확인
        if (!application.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 지원 내역만 조회할 수 있습니다.");
        }

        return new ApplicationResponseDto(application);
    }

    // 상태 변경
    @Transactional
    public ApplicationResponseDto updateStatus(Long userId, Long applicationId, String status) {
        if (!VALID_STATES.contains(status)) {
            throw new BadRequestException("유효하지 않은 상태값입니다.");
        }

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 지원 내역만 수정할 수 있습니다.");
        }

        application.updateStatus(status);
        return new ApplicationResponseDto(application);
    }

    // 면접 날짜 등록
    @Transactional
    public ApplicationResponseDto updateInterviewDate(Long userId, Long applicationId, LocalDate interviewDate) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 지원 내역만 수정할 수 있습니다.");
        }

        application.updateInterviewDate(interviewDate);
        return new ApplicationResponseDto(application);
    }

    // 메모 수정
    @Transactional
    public ApplicationResponseDto updateMemo(Long userId, Long applicationId, String memo) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 지원 내역만 수정할 수 있습니다.");
        }

        application.updateMemo(memo);
        return new ApplicationResponseDto(application);
    }

    // 지원 삭제
    @Transactional
    public void deleteApplication(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));

        if (!application.getUser().getId().equals(userId)) {
            throw new BadRequestException("본인의 지원 내역만 삭제할 수 있습니다.");
        }

        applicationRepository.delete(application);
    }
}
