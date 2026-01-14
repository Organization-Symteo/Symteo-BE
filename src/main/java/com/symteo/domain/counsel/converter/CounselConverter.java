package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;
import lombok.Builder;

public class CounselConverter {

    // 엔티티 -> DTO
    // AI 답변을 String으로 변환
    public static CounselResDTO.ChatMessage EntityToChatMessage(
            String answer
    ){
        return CounselResDTO.ChatMessage.builder()
                .text(answer)
                .build();
    }

    // DTO -> 엔티티
    // 사용자 질문을 chatMessage로 변환
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
