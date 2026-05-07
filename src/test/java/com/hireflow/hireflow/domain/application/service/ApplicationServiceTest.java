package com.hireflow.hireflow.domain.application.service;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.dto.ApplicationRequestDto;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    JobPostingRepository jobPostingRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ApplicationService applicationService;

    @Test
    void 중복_지원_시_예외() {
        // Given
        User user = User.builder().id(1L).build();
        JobPosting jobPosting = JobPosting.builder().id(1L).build();
        ApplicationRequestDto dto = new ApplicationRequestDto(1L, "Spring");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(jobPostingRepository.findById(1L)).thenReturn(Optional.of(jobPosting));
        when(applicationRepository.existsByUserIdAndJobPostingId(1L, 1L)).thenReturn(true);


        // When & Then
        assertThatThrownBy(() -> applicationService.createApplication(1L, dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 지원한 공고입니다.");
    }

    @Test
    void 타인_지원내역_조회_시_예외() {
        // Given
        User owner = User.builder().id(2L).build();
        Application application = Application.builder().user(owner).build();

        when(applicationRepository.findById(2L)).thenReturn(Optional.of(application));

        // When & Then
        assertThatThrownBy(() -> applicationService.getApplication(1L, 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("본인의 지원 내역만 조회할 수 있습니다.");

    }
}
