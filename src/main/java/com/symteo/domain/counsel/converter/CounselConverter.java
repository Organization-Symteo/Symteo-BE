package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;
import lombok.Builder;

public class CounselConverter {

    // 엔티티 -> DTO
    // AI 답변을 String으로 변환
    public static CounselResDTO.ChatMessage EntityToChat(
            Long userId,
            Long chatRoomId,
            String question,
            String answer
    ){
        return CounselResDTO.ChatMessage.builder()
                .userId(userId)
                .chatRoomId(chatRoomId)
                .userRequest(question)
                .AiResponse(answer)
                .build();
    }

    // DTO -> 엔티티
    // 1. 사용자 정보를 chatRoom으로 변환
    public static ChatRoom toChatRoom(
            Long userId
    ){
        return ChatRoom.builder()
                .userId(userId)
                .build();
    }

    // 2. 사용자 질문을 chatMessage로 변환
    public static ChatMessage toChatMessage(
            String message,
            ChatRoom chatRoom
    ){
        return ChatMessage.builder()
                .message(message)
                .chatRoom(chatRoom)
                .build();
    }
}
