package com.symteo.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AiModelDebugConfig {

    private final ChatModel chatModel;

    @PostConstruct
    void checkModel() {
        System.out.println("[AI DEBUG] ChatModel impl = " + chatModel.getClass().getName());
    }
}
