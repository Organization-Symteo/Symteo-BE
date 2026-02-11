package com.symteo.domain.report.service;

import com.symteo.domain.report.exception.ReportsErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;
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

    @Value("${openai.api.key}")
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

            // 요청 바디 구성
            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-4o");

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

            // 파싱 실패 시 예외 던짐 (피드백 반영)
            throw new GeneralException(ReportsErrorCode._AI_ANALYSIS_FAILED);

        } catch (Exception e) {
            // 에러 문자열을 반환하지 않고 예외를 전파하여 상위 서비스에서 리포트 생성을 중단하게 함
            System.err.println("OpenAI API 호출 에러: " + e.getMessage());
            throw new GeneralException(ReportsErrorCode._AI_ANALYSIS_FAILED);
        }
    }
}