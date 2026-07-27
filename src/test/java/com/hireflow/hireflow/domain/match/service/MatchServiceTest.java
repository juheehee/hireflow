package com.hireflow.hireflow.domain.match.service;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.match.Match;
import com.hireflow.hireflow.domain.match.MatchRepository;
import com.hireflow.hireflow.domain.match.MatchService;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock
    MatchRepository matchRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    JobPostingRepository jobPostingRepository;

    @InjectMocks
    MatchService matchService;

    @Test
    void 기술스택확정_안함() {
        // Given (준비)
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .techStack(null)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When (실행)
        matchService.calculateForUser(userId);

        // Then (검증)
        verify(matchRepository, never()).save(any());
        verify(jobPostingRepository, never()).findAll();
    }

    @Test
    void 기술스택_빈문자열_스킵() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .techStack(" ")
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        matchService.calculateForUser(userId);

        verify(matchRepository, never()).save(any());
        verify(jobPostingRepository, never()).findAll();
    }

    @Test
    void 정상케이스_전체공고에대해_계산됨() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .techStack("Java,Spring,PostgresSQL")
                .build();

        JobPosting posting1 = JobPosting.builder()
                .id(10L)
                .techStackTags("Java")
                .build();
        JobPosting posting2 = JobPosting.builder()
                .id(11L)
                .techStackTags("Python")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jobPostingRepository.findAll()).thenReturn(List.of(posting1, posting2));
        when(matchRepository.findByUserIdAndJobPostingId(any(), any())).thenReturn(Optional.empty());

        matchService.calculateForUser(userId);

        verify(matchRepository, times(2)).save(any(Match.class));
    }

    @Test
    void 기존매치_있으면_업데이트됨() {
        Long userId = 1L;
        User user = User.builder().id(userId).techStack("Java").build();
        JobPosting posting = JobPosting.builder().id(10L).techStackTags("Java").build();

        Match existingMatch = mock(Match.class); // 기존에 이미 저장된 Match라고 가정

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jobPostingRepository.findAll()).thenReturn(List.of(posting));
        when(matchRepository.findByUserIdAndJobPostingId(userId, 10L)).thenReturn(Optional.of(existingMatch)); // 이미 있음!

        matchService.calculateForUser(userId);

        verify(existingMatch).update(anyInt(), anyString()); // update가 호출됐는지
        verify(matchRepository, never()).save(any());        // save는 안 불렸는지
    }

    @Test
    void 점수계산_부분매칭() {
        Long userId = 1L;
        User user = User.builder().id(userId).techStack("Java,Spring,PostgreSQL,Redis").build();
        JobPosting posting = JobPosting.builder().id(10L).techStackTags("Java,Spring").build();
        // 유저 4개 중 2개(Java,Spring) 매칭 → 50점 기대

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jobPostingRepository.findAll()).thenReturn(List.of(posting));
        when(matchRepository.findByUserIdAndJobPostingId(userId, 10L)).thenReturn(Optional.empty());

        ArgumentCaptor<Match> captor = ArgumentCaptor.forClass(Match.class);

        matchService.calculateForUser(userId);

        verify(matchRepository).save(captor.capture()); // save에 실제 전달된 Match를 붙잡음
        Match savedMatch = captor.getValue();
        assertThat(savedMatch.getScore()).isEqualTo(50);
    }

    @Test
    void 존재하지않는_유저_예외() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.calculateForUser(userId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("유저를 찾을 수 없습니다.");
    }
}
