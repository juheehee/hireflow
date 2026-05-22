package com.hireflow.hireflow.infra.crawler;

import com.hireflow.hireflow.domain.jobposting.JobPosting;
import com.hireflow.hireflow.domain.jobposting.repository.JobPostingRepository;
import com.hireflow.hireflow.infra.ai.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobCrawlerService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) "
                    + "Chrome/120.0.0.0 Safari/537.36";
    private static final String SARAMIN_HOME = "https://www.saramin.co.kr";
    /** 사람인 「전체 채용정보」 영역만 (페이지당 최대 50건) */
    private static final String LIST_SELECTOR = "#default_list_wrap .list_item";

    private final JobPostingRepository jobPostingRepository;
    private final OpenAiService openAiService;

    /** 06:00, 12:00 KST — 목록 HTML만 수집 (상세·AI 태그는 30분 뒤 배치) */
    @Scheduled(cron = "0 0 6,12 * * *")
    @Transactional
    @CacheEvict(value = {"jobPostings", "recommendations"}, allEntries = true)
    public void crawlSaramin() {
        log.info("[크롤러] 사람인 크롤링 시작");

        try {
            int page = 1;
            String url = buildListUrl(page);

            Document doc = fetchSaraminListPage(url);
            logSaraminDiagnostics(doc);

            Elements jobs = doc.select(LIST_SELECTOR);
            log.info("[크롤러] 파싱 대상 공고 카드 수 ({}): {}", LIST_SELECTOR, jobs.size());

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

                    LocalDate deadline = LocalDate.now().plusDays(30); // 파싱 실패 시 기본값
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

                    String sourceUrl = job.selectFirst("div.job_tit a") != null
                            ? SARAMIN_HOME + job.selectFirst("div.job_tit a").attr("href")
                            : "";

                    if (title.isBlank() || company.isBlank() || sourceUrl.isBlank()) {
                        continue;
                    }

                    // 중복 체크
                    if (jobPostingRepository.findBySourceUrl(sourceUrl).isPresent()) {
                        skipped++;
                        continue;
                    }

                    // 목록만 먼저 저장 — description/태그는 updateMissingDescriptions 배치에서 처리
                    JobPosting posting = JobPosting.builder()
                            .title(title)
                            .company(company)
                            .location(location)
                            .description("")
                            .techStackTags("")
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

            log.info("[크롤러] 완료 — 저장 {}건 / 중복 스킵 {}건 (page={})", saved, skipped, page);

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

    private String buildListUrl(int page) {
        return SARAMIN_HOME + "/zf_user/jobs/list/job-category"
                + "?page=" + page
                + "&cat_kewd=84&tab_type=all&search_optional_item=n"
                + "&search_done=y&panel_count=y&isAjaxRequest=0&page_count=50"
                + "&sort=RL&type=job-category&is_param=1&isSearchResultEmpty=1"
                + "&isSectionHome=0&searchParamCount=1";
    }

    /** 메인 방문 후 쿠키 유지 → 리스트 페이지 요청 (브라우저 흐름 모사) */
    private Document fetchSaraminListPage(String listUrl) throws IOException {
        Connection.Response homeResponse = Jsoup.connect(SARAMIN_HOME)
                // Accept-Encoding의 br = Brotli 압축 (CSS <br> 아님). Jsoup은 br 디코딩이 불안정해 제외
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip, deflate")
                .timeout(10_000)
                .method(Connection.Method.GET)
                .execute();

        Map<String, String> cookies = homeResponse.cookies();
        log.info("[크롤러/진단] 메인 접속 status={}, cookieKeys={}",
                homeResponse.statusCode(), cookies.keySet());

        Connection.Response listResponse = Jsoup.connect(listUrl)
                .userAgent(USER_AGENT)
                .cookies(cookies)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Accept-Encoding", "gzip, deflate")
                .header("Connection", "keep-alive")
                .referrer(SARAMIN_HOME)
                .timeout(10_000)
                .method(Connection.Method.GET)
                .execute();

        log.info("[크롤러/진단] 리스트 접속 status={}, finalUrl={}",
                listResponse.statusCode(), listResponse.url());

        return listResponse.parse();
    }

    private void logSaraminDiagnostics(Document doc) {
        int bodyLen = doc.body() != null ? doc.body().html().length() : 0;
        int allListItems = doc.select("div.list_item").size();
        int scopedListItems = doc.select(LIST_SELECTOR).size();
        int boxItems = doc.select("div.box_item").size();
        String totalCount = doc.select(".list_total_count .total_count em").text();
        boolean hasDefaultWrap = doc.selectFirst("#default_list_wrap") != null;
        String bodyPreview = doc.body() != null
                ? doc.body().text().replaceAll("\\s+", " ").substring(0, Math.min(200, doc.body().text().length()))
                : "";

        log.info("[크롤러/진단] bodyLen={}, totalCount='{}', #default_list_wrap={}",
                bodyLen, totalCount, hasDefaultWrap);
        log.info("[크롤러/진단] selector 비교 — div.list_item={}, {}={}, div.box_item={}",
                allListItems, LIST_SELECTOR, scopedListItems, boxItems);
        log.info("[크롤러/진단] body 텍스트 미리보기: {}", bodyPreview);

        if (scopedListItems == 0) {
            log.warn("[크롤러/진단] 전체 채용 0건 — 차단 HTML 또는 셀렉터 불일치 가능. bodyLen={}", bodyLen);
        }
    }

    private String parseDescription(String sourceUrl) {
        try {
            // rec_idx 추출 후 상세 iframe URL 조회
            String recIdx = sourceUrl.contains("rec_idx=")
                    ? sourceUrl.split("rec_idx=")[1].split("&")[0]
                    : "";
            if (recIdx.isBlank()) return "";

            String iframeUrl = SARAMIN_HOME + "/zf_user/jobs/relay/view-detail"
                    + "?rec_idx=" + recIdx + "&rec_seq=0";

            Connection.Response response = Jsoup.connect(iframeUrl)
                    .userAgent(USER_AGENT)
                    .header("Accept-Encoding", "gzip, deflate")
                    .referrer(sourceUrl)
                    .timeout(10_000)
                    .execute();

            String text = response.parse().body().text();
            return text.length() > 1000 ? text.substring(0, 1000) : text;
        } catch (Exception e) {
            log.warn("[크롤러] description 파싱 실패 - {}", e.getMessage());
            return "";
        }
    }
}
