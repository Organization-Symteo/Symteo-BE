package com.symteo.domain.counsel.dto;

import lombok.Builder;

public class AISummaryDTO {

    // AI에게 보내는 채팅 묶음
    @Builder
    public record ChatMessage(
            String chatMessages,
            String userMessages,
            String aiMessages
    ){}
}
