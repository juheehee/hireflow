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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerService {

    private final JobPostingRepository jobPostingRepository;
    private final OpenAiService openAiService;

    /** 06:00, 12:00 KST — 목록 HTML만 수집 (상세·AI 태그는 30분 뒤 배치) */
    @Scheduled(cron = "0 0 6,12 * * *")
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
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
                    String title = job.select("a[href*=relay/view]").attr("title");
                    String company = job.select("span.corp").text();
                    // location — ul.desc 안에서 첫번째 li (근무지)
                    String location = job.select("ul.desc li").first() != null
                            ? job.select("ul.desc li").first().text() : "";

                    LocalDate deadline = LocalDate.now().plusDays(30); // 기본값
                    Element dateEl = job.selectFirst("span.date");
                    if (dateEl != null) {
                        try {
                            String dateText = dateEl.text().trim();

                            if (dateText.contains("내일")) {
                                deadline = LocalDate.now().plusDays(1);
                            } else if (dateText.contains("오늘")) {
                                deadline = LocalDate.now();
                            } else if (dateText.matches(".*D-\\d+.*")) {
                                // "D-4" → 4일 후
                                int days = Integer.parseInt(dateText.replaceAll(".*D-(\\d+).*", "$1"));
                                deadline = LocalDate.now().plusDays(days);
                            } else if (dateText.contains("~")) {
                                // "~05.31(일)" → 날짜 파싱
                                String cleaned = dateText
                                        .replaceAll("~", "")
                                        .replaceAll("\\(.*\\)", "")
                                        .trim();
                                String[] parts = cleaned.split("\\.");
                                int month = Integer.parseInt(parts[0]);
                                int day = Integer.parseInt(parts[1]);
                                int year = LocalDate.now().getYear();
                                if (month < LocalDate.now().getMonthValue()) year++;
                                deadline = LocalDate.of(year, month, day);
                            }
                        } catch (Exception e) {
                            log.warn("[크롤러] 마감일 파싱 실패 — {}", e.getMessage());
                        }
                    }

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

                    String description = parseDescription(sourceUrl);

                    String techStackTags = "";
                    try {
                        techStackTags = openAiService.extractTechStackFromJobPosting(title, company, description);
                    } catch (Exception e) {
                        log.warn("[크롤러] OpenAI 태그 추출 실패 — {}", e.getMessage());
                    }

                    JobPosting posting = JobPosting.builder()
                            .title(title)
                            .company(company)
                            .location(location)
                            .description(description)
                            .techStackTags(techStackTags)
                            .deadline(deadline)
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

            log.info("[크롤러] 완료 — 저장 {}건 / 중복 스킵 {}건)", saved, skipped);

        } catch (Exception e) {
            log.error("[크롤러] 사람인 접속 실패 — {}", e.getMessage(), e);
        }
    }

    /** 06:30, 12:30 KST — description·기술스택 태그가 비어 있는 공고만 상세 URL 순차 조회 */
    @Scheduled(cron = "0 30 6,12 * * *")
    @Transactional
    public void updateMissingDescriptions() {
        log.info("[배치] description 없는 공고 업데이트 시작");

        List<JobPosting> targets = jobPostingRepository
                .findByDescriptionIsEmptyAndSourceNot(JobSource.MANUAL);

        int updated = 0;
        for (JobPosting posting : targets) {
            String description = parseDescription(posting.getSourceUrl());
            if (!description.isBlank()) {
                posting.updateDescription(description);

                // description 있으면 techStackTags도 재추출
                try {
                    String tags = openAiService.extractTechStackFromJobPosting(
                            posting.getTitle(), posting.getCompany(), description);
                    posting.updateTechStackTags(tags);
                } catch (Exception e) {
                    log.warn("[배치] OpenAI 태그 재추출 실패 - {}", e.getMessage());
                }
                updated++;
            }

            // 사람인 부하·봇 탐지 완화
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }

        log.info("[배치] 완료 - {}건 업데이트", updated);
    }

    private String parseDescription(String sourceUrl) {
        try {
            // rec_idx 추출
            String recIdx = sourceUrl.contains("rec_idx=")
                    ? sourceUrl.split("rec_idx=")[1].split("&")[0]
                    : "";
            if (recIdx.isBlank()) return "";

            String iframeUrl = "https://www.saramin.co.kr/zf_user/jobs/relay/view-detail"
                    + "?rec_idx=" + recIdx + "&rec_seq=0";

            Document iframeDoc = Jsoup.connect(iframeUrl)
                    .userAgent("Mozilla/5.0 (windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10_000)
                    .get();

            String text = iframeDoc.body().text();
            return text.length() > 1000 ? text.substring(0, 1000) : text;
        } catch (Exception e) {
            log.warn("[크롤러] description 파싱 실패 - {}", e.getMessage());
            return "";
        }
    }
}
