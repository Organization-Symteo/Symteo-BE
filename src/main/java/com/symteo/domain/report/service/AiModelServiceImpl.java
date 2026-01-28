package com.symteo.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.key}") // application.yml에 등록된 키를 읽어옵니다.
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Override
    public String callAiApi(String prompt) {
        try {
            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            // 요청 바디 구성 (GPT-4o 또는 gpt-3.5-turbo 사용)
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o"); // 혹은 gpt-3.5-turbo

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));
            body.put("messages", messages);
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // API 호출
            Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);

            // 응답 파싱
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }

            return "AI 응답을 파싱하는 데 실패했습니다.";

        } catch (Exception e) {
            System.err.println("OpenAI API 호출 에러: " + e.getMessage());
            return "AI 분석 도중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}