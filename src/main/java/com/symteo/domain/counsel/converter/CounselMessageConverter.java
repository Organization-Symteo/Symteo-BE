package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.dto.redis.ChatMessageCache;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.List;
import java.util.stream.Collectors;

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

    // API : 채팅 종료하기 API
    // 단일 캐시 메세지를 JAVA ChatMessage Entity로 변환하기
    // Redis 캐시를 MySQL 엔티티 변환하기.
    public static ChatMessage toChatMessage(ChatMessageCache message, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .role(message.role())
                .message(message.content())
                .build();
    }

    // 전체 캐시 메세지를 JAVA ChatMessage의 리스트로 변환하기
    // Redis 캐시를 MySQL 엔티티 목록으로 변환하기.
    public static List<ChatMessage> toChatMessageList(List<ChatMessageCache> messageList, ChatRoom chatRoom) {
        return messageList.stream()
                .map(cache -> toChatMessage(cache, chatRoom))
                .toList();
    }


    // 채팅 종료하기
    // Redis 캐시 메세지 리스트를 OpenAI 요약 요청용 텍스트 변환하기.
    public static String toSummary(List<ChatMessageCache> caches) {
        return caches.stream()
                .map(cache -> cache.role().name() + ": " + cache.content())
                .collect(Collectors.joining("\n"));
    }
}
