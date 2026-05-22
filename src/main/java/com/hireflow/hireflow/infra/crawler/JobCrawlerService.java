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

    @Scheduled(cron = "0 0 6,12 * * *")
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
    public void crawlSaramin() {
        log.info("[크롤러] 사람인 크롤링 시작");

        try {
            String url = "https://www.saramin.co.kr/zf_user/jobs/list/job-category" +
                    "?page=1&cat_kewd=84&tab_type=all&search_optional_item=n" +
                    "&search_done=y&panel_count=y&isAjaxRequest=0&page_count=50" +
                    "&sort=RL&type=job-category&is_param=1&isSearchResultEmpty=1" +
                    "&isSectionHome=0&searchParamCount=1";

//            String url = "https://www.saramin.co.kr/zf_user/jobs/list/job-category" +
//                    "?cat_kewd=84&panel_type=&search_optional_item=n" +
//                    "&search_done=y&panel_count=y&preview=y";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Connection", "keep-alive")
                    .referrer("https://www.saramin.co.kr")
                    .timeout(10_000)
                    .get();

            Elements jobs = doc.select("div.list_item");  // 사람인 (백엔드/ 서버 개발) 전체 채용 정보 리스트
            log.info("[크롤러] 공고 카드 수: {}", jobs.size());
            log.info("[크롤러] HTML 일부: {}", doc.body().html().substring(0, 2000));

            int saved = 0;
            int skipped = 0;

            for (Element job : jobs) {
                try {
                    String title = job.selectFirst("div.job_tit a") != null
                            ? job.selectFirst("div.job_tit a").attr("title") : "";
                    String company = job.selectFirst("div.company_nm .str_tit") != null
                            ? job.selectFirst("div.company_nm .str_tit").text() : "";
                    String location = job.selectFirst("p.work_place") != null
                            ? job.selectFirst("p.work_place").text() : "";

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

                    String sourceUrl  = job.selectFirst("div.job_tit a") != null
                            ? "https://www.saramin.co.kr" + job.selectFirst("div.job_tit a").attr("href")
                            : "";

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

            log.info("[크롤러] 완료 — 저장 {}건 / 중복 스킵 {}건", saved, skipped);

        } catch (Exception e) {
            log.error("[크롤러] 사람인 접속 실패 — {}", e.getMessage());
        }
    }

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

            // 사람인 부하 방지
            try {Thread.sleep(500);} catch (InterruptedException ignored) {}
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
