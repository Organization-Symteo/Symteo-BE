package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.entity.ChatMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;

public class CounselMessageConverter {
    public static List<Message> toAiMessages(List<ChatMessage> historyDesc) {

        // DB에서 DESC로 가져온 걸, LLM 입력용으로 ASC(오래된→최신)로 정렬
        return historyDesc.stream()
                .map(CounselMessageConverter::toAiMessage)
                .toList();
    }

    // AI Server가 읽을 수 있게 메세지 분별하기.
    private static Message toAiMessage(ChatMessage m) {
        return switch (m.getRole()) {
            case USER -> new UserMessage(m.getMessage());
            case AI, REPORT -> new AssistantMessage(m.getMessage());
            case ADMIN -> new SystemMessage(m.getMessage());
        };
    }
}
