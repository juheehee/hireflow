package com.hireflow.hireflow.domain.coverletter.service;

import com.hireflow.hireflow.domain.application.Application;
import com.hireflow.hireflow.domain.application.repository.ApplicationRepository;
import com.hireflow.hireflow.domain.coverletter.CoverLetter;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterDraftRequestDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterRequestDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterResponseDto;
import com.hireflow.hireflow.domain.coverletter.dto.CoverLetterScoreResponseDto;
import com.hireflow.hireflow.domain.coverletter.repository.CoverLetterRepository;
import com.hireflow.hireflow.global.exception.NotFoundException;
import com.hireflow.hireflow.global.exception.UnauthorizedException;
import com.hireflow.hireflow.infra.ai.CoverLetterScoreResult;
import com.hireflow.hireflow.infra.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final ApplicationRepository applicationRepository;
    private final OpenAiService openAiService;

    // 자소서 작성
    @Transactional
    public CoverLetterResponseDto create(Long userId, Long applicationId, CoverLetterRequestDto dto) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));
        validateOwner(application, userId);

        CoverLetter coverLetter = CoverLetter.builder()
                .content(dto.getContent())
                .application(application)
                .createdAt(LocalDateTime.now())
                .build();

        return new CoverLetterResponseDto(coverLetterRepository.save(coverLetter));
    }

    // 자소서 목록 조회
    @Transactional(readOnly = true)
    public List<CoverLetterResponseDto> getList(Long userId, Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));
        validateOwner(application, userId);

        return coverLetterRepository.findByApplicationId(applicationId).stream()
                .map(CoverLetterResponseDto::new)
                .toList();
    }

    // 자소서 수정
    @Transactional
    public CoverLetterResponseDto update(Long userId, Long applicationId, Long coverLetterId, CoverLetterRequestDto dto) {
        CoverLetter coverLetter = getCoverLetter(userId, applicationId, coverLetterId);
        coverLetter.update(dto.getContent());
        return new CoverLetterResponseDto(coverLetter);
    }

    // 자소서 삭제
    @Transactional
    public void delete(Long userId, Long applicationId, Long coverLetterId) {
        CoverLetter coverLetter = getCoverLetter(userId, applicationId, coverLetterId);
        coverLetterRepository.delete(coverLetter);
    }

    // AI 채점
    @Transactional
    public CoverLetterScoreResponseDto score(Long userId, Long applicationId, Long coverLetterId) {
        CoverLetter coverLetter = getCoverLetter(userId, applicationId, coverLetterId);
        String jobDescription = coverLetter.getApplication().getJobPosting().getDescription();

        CoverLetterScoreResult result = openAiService.scoreCoverLetter(coverLetter.getContent(), jobDescription);
        coverLetter.updateScore(result.score(), result.feedback());

        return new CoverLetterScoreResponseDto(result.score(), result.feedback(),
                result.strengths(), result.improvements());
    }

    // AI 초안 생성
    @Transactional
    public CoverLetterResponseDto draft(Long userId, Long applicationId, CoverLetterDraftRequestDto dto) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));
        validateOwner(application, userId);

        String jobDescription = application.getJobPosting().getDescription();
        String generated = openAiService.generateDraft(dto.getQuestion(), dto.getPrompt(), jobDescription);

        CoverLetter coverLetter = CoverLetter.builder()
                .content(generated)
                .application(application)
                .createdAt(LocalDateTime.now())
                .build();

        return new CoverLetterResponseDto(coverLetterRepository.save(coverLetter));
    }

    // AI 첨삭
    @Transactional
    public CoverLetterResponseDto feedback(Long userId, Long applicationId, Long coverLetterId) {
        CoverLetter coverLetter = getCoverLetter(userId, applicationId, coverLetterId);
        String jobDescription = coverLetter.getApplication().getJobPosting().getDescription();

        String improved = openAiService.refineCoverLetter(coverLetter.getContent(), jobDescription);
        coverLetter.updateFeedback(improved);

        return new CoverLetterResponseDto(coverLetter);
    }

    // 공통 - 자소서 조회 + 소유자 검증
    private CoverLetter getCoverLetter(Long userId, Long applicationId, Long coverLetterId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new NotFoundException("지원 내역을 찾을 수 없습니다."));
        validateOwner(application, userId);

        CoverLetter coverLetter = coverLetterRepository.findById(coverLetterId)
                .orElseThrow(() -> new NotFoundException("자소서를 찾을 수 없습니다."));

        if (!coverLetter.getApplication().getId().equals(applicationId)) {
            throw new UnauthorizedException("해당 자소서에 접근할 권한이 없습니다.");
        }

        return coverLetter;
    }

    private void validateOwner(Application application, Long userId) {
        if (!application.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("해당 지원 내역에 접근할 권한이 없습니다.");
        }
    }
}
