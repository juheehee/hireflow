package com.hireflow.hireflow.infra.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String extractTechStackFromJobPosting(String title, String company, String description) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String content = description.isBlank()
                ? title + " " + company
                : title + " " + company + "\n" + description;

        String prompt = """
                아래는 채용공고 정보입니다.
                이 공고에서 요구하는 기술 스택만 추출해서 쉼표로 구분된 문자열로 반환해주세요.
                예시: Java, Spring Boot, PostgreSQL, Redis
                다른 설명 없이 기술 스택 목록만 반환해주세요.
                
                채용공고:
                %s
                """.formatted(content);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }

    public String parseResumeToTechStack(String resumeText) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        String prompt = """
                아래는 지원자의 이력서 내용입니다.
                이력서에서 기술 스택만 추출해서 쉼표로 구분된 문자열로 반환해주세요.
                예시: Java, Spring Boot, PostgreSQL, Redis, AWS S3
                다른 설명 없이 기술 스택 목록만 반환해주세요.
                
                이력서:
                %s
                """.formatted(resumeText);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 200
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }

    public CoverLetterScoreResult scoreCoverLetter(String content, String jobDescription) {
        String context = (jobDescription == null || jobDescription.isBlank())
                ? "공고 정보 없음"
                : jobDescription;

        String prompt = """
                당신은 채용 전문가입니다. 아래 자소서를 100점 만점으로 채점하고 피드백을 제공해주세요.
                
                [채점 기준]
                - 직무 연관성 (30점): 공고에서 요구하는 역량과 자소서 내용의 연결성
                - 구체성 (30점): 경험에 수치/지표가 포함되어 있는지, 모호한 표현 없이 구체적인지
                - 논리적 흐름(20점): 상황->판단->행동->결과 흐름이 자연스러운지
                - 표현 완성도(20점): 문장이 간결하고 명확한지, 나열식 서술을 피했는지
                
                [출력 형식]
                반드시 아래 JSON 형식으로만 응답하세요. 다른 텍스트나 마크다운 코드블록 없이 JSON만 반환하세요.
                {
                    "score": 숫자,
                    "feedback": "전반적인 피드백",
                    "strengths": ["잘된 점1", "잘된 점2"],
                    "improvements": ["개선할 점1". "개선할 점2"]
                }
                
                [채용공고]
                %s
                
                [자소서]
                %s
                """.formatted(context, content);

        String rawContent = callOpenAi(prompt, 600);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(rawContent);
            int score = node.get("score").asInt();
            String feedback = node.get("feedback").asText();
            List<String> strengths = mapper.convertValue(node.get("strengths"),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            List<String> improvements = mapper.convertValue(node.get("improvements"),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            return new CoverLetterScoreResult(score, feedback, strengths, improvements);
        } catch (Exception e) {
            throw new RuntimeException("AI 채점 결과 파싱 실패: " + rawContent);
        }
    }

    public String generateDraft(String question, String userPrompt, String jobDescription) {
        String context = (jobDescription == null || jobDescription.isBlank())
                ? "공고 정보 없음"
                : jobDescription;

        String prompt = """
                당신은 취업 자소서 작성 전문가입니다. 아래 정보를 바탕으로 자소서 초안을 작성해주세요.
                
                [작성 원칙]
                - 없는 사실이나 수치를 절대 만들어내지 마세요. 지원자가 제공한 내용만 활용하세요
                - 경험을 나열하지 말고 상황->판단->행동->결과 흐름으로 구조화하세요
                - 구체적인 수치나 성과를 포함하세요
                - 두괄식으로 핵심 메시지를 먼저 제시하세요
                - 자연스럽고 진솔한 어조로 작성하세요
                - 800자 내외로 작성하세요
                
                [자소서 문항]
                %s
                
                [지원자가 강조하고 싶은 내용]
                %s
                
                [채용공고]
                %s
                
                위 정보를 바탕으로 자소서 본문만 작성해주세요. 제목이나 부가 설명 없이 본문만  반환하세요.
                """.formatted(question, userPrompt, context);

        return callOpenAi(prompt, 1000);
    }

    public String refineCoverLetter(String content, String jobDescription) {
        String context = (jobDescription == null || jobDescription.isBlank())
                ? "공고 정보 없음"
                : jobDescription;

        String prompt = """
                당신은 취업 자소서 첨삭 전문가입니다. 아래 자소서를 개선해주세요.
                
                [첨삭 원칙]
                - 없는 사실이나 수치를 절대 만들어내지 마세요. 원문에 있는 내용만 활용하세요
                - 경험 나열을 상황->판단->행동->결과 흐름으로 재구성하세요.
                - 모호한 표현은 구체적으로 바꾸세요
                - 공고와 연관성이 낮은 내용은 제거하거나 연결점을 강화하세요
                - 원문의 핵심 경험과 메시지는 유지하세요
                - 원문과 비슷한 분량으로 작성하세요
                
                [채용공고]
                %s
                
                [원본 자소서]
                %s
                
                개선된 자소서 본문만 반환하세요. 설명이나 부가 텍스트 없이 본문만 반환하세요.
                """.formatted(context, content);

        return callOpenAi(prompt, 1000);
    }

    private String callOpenAi(String prompt, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", maxTokens
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, request, Map.class);

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return message.get("content").toString().trim();
    }
}
