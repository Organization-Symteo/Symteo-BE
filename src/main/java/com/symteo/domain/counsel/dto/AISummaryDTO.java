package com.symteo.domain.counsel.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

public class AISummaryDTO {

    // AI에게 보내는 채팅 묶음
    @Builder
    public record ChatMessage(
            @JsonProperty("chatMessages") String chatMessages,
            @JsonProperty("userMessages") String userMessages,
            @JsonProperty("aiMessages") String aiMessages
    ){}
}
