package com.symteo.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Profile("!test")
@Configuration
public class AiConfig {
    @Value("${spring.ai.openai.api-key:}")
    private String openAiKey;

    @PostConstruct
    void checkKey() {
        System.out.println("[AI DEBUG] openAiKey length = " +
                (openAiKey == null ? "null" : openAiKey.length()));
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
