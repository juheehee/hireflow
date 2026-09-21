package com.hireflow.hireflow.domain.coverletter.service;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.coverletter.CoverLetter;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterRequestDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterResponseDto;
import com.hireflow.hireflow.domain.coverletter.repository.CoverLetterRepository;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.global.exception.NotFoundException;
import com.hireflow.hireflow.global.exception.UnauthorizedException;
import com.hireflow.hireflow.infra.ai.OpenAiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoverLetterServiceTest {

    @Mock
    private CoverLetterRepository coverLetterRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private OpenAiService openAiService;

    @InjectMocks
    private CoverLetterService coverLetterService;

    @Test
    void 본인_소유_지원내역이면_수정_성공() {
        // given(준비)
        Long userId = 1L;
        Long applicationId = 10L;
        Long coverLetterId = 100L;

        User user = User.builder()
                .id(userId)
                .build();

        Application application = Application.builder()
                .id(applicationId)
                .user(user)
                .build();

        CoverLetter coverLetter = CoverLetter.builder()
                .id(coverLetterId)
                .application(application)
                .content("기존 내용")
                .createdAt(LocalDateTime.now())
                .build();

        CoverLetterRequestDto dto = new CoverLetterRequestDto("수정된 내용");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(coverLetterRepository.findById(coverLetterId)).thenReturn(Optional.of(coverLetter));

        // when(실행)
        CoverLetterResponseDto result = coverLetterService.update(userId, applicationId, coverLetterId, dto);

        // then(검증)
        assertThat(result.getContent()).isEqualTo("수정된 내용");
    }

    @Test
    void 타인_소유_지원내역이면_수정시_예외발생() {
        // given
        Long ownerUserId = 1L; // 실제 지원서 소유자
        Long requesterUserId = 2L; // 요청한 사람 (다른 사람!)
        Long applicationId = 10L;
        Long coverLetterId = 100L;

        User owner = User.builder()
                .id(ownerUserId)
                .build();

        Application application = Application.builder()
                .id(applicationId)
                .user(owner)
                .build();

        CoverLetterRequestDto dto = new CoverLetterRequestDto("수정 시도");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.update(requesterUserId, applicationId, coverLetterId, dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("해당 지원 내역에 접근할 권한이 없습니다.");
    }

    @Test
    void 본인소유_다른지원서의_자소서_접근시_예외발생() {
        // given
        Long userId = 1L;
        Long requestedApplicationId = 10L; // 요청 시 넘긴 applicationId
        Long coverLetterId = 100L;

        User user = User.builder()
                .id(userId)
                .build();

        Application requestedApplication = Application.builder()
                .id(requestedApplicationId) // 요청자가 지목한 application (본인 소유)
                .user(user)
                .build();

        Application actualOwnerApplication = Application.builder()
                .id(20L) // coverLetter가 실제로 속한 application (다른 id)
                .user(user) // 같은 유저 소유이긴 함
                .build();

        CoverLetter coverLetter = CoverLetter.builder()
                .id(coverLetterId)
                .application(actualOwnerApplication) // applicationId = 20에 속해있음
                .content("기존 내용")
                .build();

        CoverLetterRequestDto dto = new CoverLetterRequestDto("수정 시도");

        when(applicationRepository.findById(requestedApplicationId)).thenReturn(Optional.of(requestedApplication));
        when(coverLetterRepository.findById(coverLetterId)).thenReturn(Optional.of(coverLetter));

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.update(userId, requestedApplicationId, coverLetterId, dto))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("해당 자소서에 접근할 권한이 없습니다.");
    }

    @Test
    void 존재하지않는_지원내역이면_예외발생() {
        // given
        Long userId = 1L;
        Long applicationId = 999L;
        Long coverLetterId = 100L;
        CoverLetterRequestDto dto = new CoverLetterRequestDto("수정 시도");

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.update(userId, applicationId, coverLetterId, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("지원 내역을 찾을 수 없습니다.");
    }

    @Test
    void 존재하지않는_자소서면_예외발생() {
        // given
        Long userId = 1L;
        Long applicationId = 10L;
        Long coverLetterId = 999L;
        CoverLetterRequestDto dto = new CoverLetterRequestDto("수정 시도");

        User user = User.builder().id(userId).build();
        Application application = Application.builder().id(applicationId).user(user).build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(coverLetterRepository.findById(coverLetterId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.update(userId, applicationId, coverLetterId, dto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("자소서를 찾을 수 없습니다.");
    }

    @Test
    void 삭제_타인소유면_예외발생() {
        // given
        Long ownerUserId = 1L;
        Long requesterUserId = 2L;
        Long applicationId = 10L;
        Long coverLetterId = 100L;

        User owner = User.builder()
                .id(ownerUserId)
                .build();

        Application application = Application.builder()
                .id(applicationId)
                .user(owner)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.delete(requesterUserId, applicationId, coverLetterId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("해당 지원 내역에 접근할 권한이 없습니다.");
    }

    @Test
    void 채점_타인소유면_예외발생() {
        // given
        Long ownerUserId = 1L;
        Long requesterUserId = 2L;
        Long applicationId = 10L;
        Long coverLetterId = 100L;

        User owner = User.builder()
                .id(ownerUserId)
                .build();

        Application application = Application.builder()
                .id(applicationId)
                .user(owner)
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        // when & then
        assertThatThrownBy(() ->
                coverLetterService.score(requesterUserId, applicationId, coverLetterId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("해당 지원 내역에 접근할 권한이 없습니다.");
    }
}
