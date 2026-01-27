package com.symteo.domain.report.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final RestTemplate restTemplate;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Override
    public String callAiApi(String prompt) {
        try {
            // 서비스 로직 완성을 위한 구조를 제공.
            return "생성된 AI 심리 분석 답변입니다.";
        } catch (Exception e) {
            return "AI 분석 도중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        }
    }
}