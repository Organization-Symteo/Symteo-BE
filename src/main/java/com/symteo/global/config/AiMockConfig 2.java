package com.symteo.global.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
@Profile("test")
public class AiMockConfig {

    @Bean
    @Primary
    public ChatModel fakeChatModel() {
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                simulateLatency();
                String content = resolveMockResponse(prompt);

                return new ChatResponse(List.of(new Generation(new AssistantMessage(content))));
            }


            private void simulateLatency() {
                // 평균 5초, 약간의 변동성
                long delayMs = ThreadLocalRandom.current().nextLong(4500, 5500);
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            private String resolveMockResponse(Prompt prompt) {
                String promptText = prompt.toString().toLowerCase();

                // 1. 요약 요청 (AISummaryDTO.ChatMessage)
                if (promptText.contains("요약") || promptText.contains("summary")) {
                    return summaryJson();
                }

                // 2. 일반 상담 응답 (AiAnswerDTO.AnswerDTO)
                return answerJson();
            }

            private String answerJson() {
                return """
                    {
                      "content": "부하 테스트용 AI 응답입니다. 실제 모델은 호출되지 않았습니다."
                    }
                    """;
            }

            private String summaryJson() {
                return """
                    {
                      "chatMessages": "대화 전반에서 정서적 스트레스 표현",
                      "userMessages": "불안과 피로감 반복 호소",
                      "aiMessages": "휴식 및 자기관리 권고"
                    }
                    """;
            }
        };
    }

    @Bean
    @Primary
    public ChatClient chatClient(ChatModel chatModel) {
        // ✅ ChatClient가 내부적으로 이 ChatModel을 사용하도록 연결
        return ChatClient.builder(chatModel).build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
