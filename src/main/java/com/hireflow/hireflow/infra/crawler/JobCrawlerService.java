package com.hireflow.hireflow.infra.crawler;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.infra.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerService {

    private final JobPostingRepository jobPostingRepository;
    private final OpenAiService openAiService;

    @Scheduled(cron = "0 0 6,12 * * *")
    public void crawlSaramin() {
        log.info("[크롤러] 사람인 크롤링 시작");

        try {
            String url = "https://www.saramin.co.kr/zf_user/jobs/list/job-category" +
                    "?cat_kewd=84&panel_type=&search_optional_item=n" +
                    "&search_done=y&panel_count=y&preview=y";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10_000)
                    .get();

            Elements jobs = doc.select("li.item.lookup");  // 사람인 공고 카드 selector

            int saved = 0;
            int skipped = 0;

            for (Element job : jobs) {
                try {
                    String title      = job.select("a[href*=relay/view]").attr("title");
                    String company  = job.select("span.corp").text();
                    // location — ul.desc 안에서 첫번째 li (근무지)
                    String location = job.select("ul.desc li").first() != null
                            ? job.select("ul.desc li").first().text() : "";
                    String sourceUrl  = "https://www.saramin.co.kr" +
                            job.select("a[href*=relay/view]").attr("href");

                    // 유효성 검사
                    if (title.isBlank() || company.isBlank() || sourceUrl.isBlank()) {
                        continue;
                    }

                    // 중복 체크
                    if (jobPostingRepository.findBySourceUrl(sourceUrl).isPresent()) {
                        skipped++;
                        continue;
                    }

                    String techStackTags = "";
                    try {
                        techStackTags = openAiService.parseResumeToTechStack(title + " " + company);
                    } catch (Exception e) {
                        log.warn("[크롤러] OpenAI 태그 추출 실패 — {}", e.getMessage());
                    }

                    JobPosting posting = JobPosting.builder()
                            .title(title)
                            .company(company)
                            .location(location)
                            .description("")          // 상세 파싱은 MVP 이후
                            .techStackTags(techStackTags)
                            .deadline(LocalDate.now().plusDays(30))  // 임시값
                            .sourceUrl(sourceUrl)
                            .source(JobSource.SARAMIN)
                            .crawledAt(java.time.LocalDateTime.now())
                            .build();

                    jobPostingRepository.save(posting);
                    saved++;

                } catch (Exception e) {
                    log.warn("[크롤러] 공고 파싱 실패 — {}", e.getMessage());
                }
            }

            log.info("[크롤러] 완료 — 저장 {}건 / 중복 스킵 {}건", saved, skipped);

        } catch (Exception e) {
            log.error("[크롤러] 사람인 접속 실패 — {}", e.getMessage());
        }
    }
}
