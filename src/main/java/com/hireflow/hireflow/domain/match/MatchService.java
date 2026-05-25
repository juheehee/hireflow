package com.hireflow.hireflow.domain.match;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.domain.user.User;
import com.hireflow.hireflow.domain.user.repository.UserRepository;
import com.hireflow.hireflow.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final JobPostingRepository jobPostingRepository;

    // 기술스택 확정 시 - 해당 유저 x 전체 공고
    @Transactional
    public void calculateForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        if (user.getTechStack() == null || user.getTechStack().isBlank()) return;

        List<String> userStacks = parseStacks(user.getTechStack());
        List<JobPosting> allPostings = jobPostingRepository.findAll();

        for (JobPosting posting : allPostings) {
            computeAndSave(user, posting, userStacks);
        }
    }

    // 크롤링 완료 후 - 기술스택 확정 유저 전체 x 신규 공고만
    @Transactional
    public void calculateForNewPostings(List<Long> newPostingIds) {
        if (newPostingIds.isEmpty()) return;

        List<User> confirmedUsers = userRepository.findByTechStackIsNotNull();
        List<JobPosting> newPostings = jobPostingRepository.findAllById(newPostingIds);

        for (User user : confirmedUsers) {
            if (user.getTechStack().isBlank()) continue;
            List<String> userStacks = parseStacks(user.getTechStack());
            for (JobPosting posting : newPostings) {
                computeAndSave(user, posting, userStacks);
            }
        }
    }

    private void computeAndSave(User user, JobPosting posting, List<String> userStacks) {
        if (posting.getTechStackTags() == null || posting.getTechStackTags().isBlank()) return;

        List<String> postingStacks = parseStacks(posting.getTechStackTags());
        int score = calculateScore(userStacks, postingStacks);
        String reason = buildReason(userStacks, postingStacks);

        matchRepository.findByUserIdAndJobPostingId(user.getId(), posting.getId())
                .ifPresentOrElse(
                        existing -> existing.update(score, reason),
                        () -> matchRepository.save(Match.create(user, posting, score, reason))
                );
    }

    private int calculateScore(List<String> userStacks, List<String> postingStacks) {
        if (userStacks.isEmpty()) return 0;
        long matched = userStacks.stream()
                .filter(s -> postingStacks.stream()
                        .anyMatch(p -> p.equalsIgnoreCase(s)))
                .count();
        return (int) (matched * 100 / userStacks.size());
    }

    private String buildReason(List<String> userStacks, List<String> postingStacks) {
        List<String> matched = userStacks.stream()
                .filter(s -> postingStacks.stream()
                        .anyMatch(p -> p.equalsIgnoreCase(s)))
                .toList();
        return matched.isEmpty() ? "매칭 기술 없음" : String.join(", ", matched) + " 매칭";
    }

    private List<String> parseStacks(String techStack) {
        return Arrays.stream(techStack.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
