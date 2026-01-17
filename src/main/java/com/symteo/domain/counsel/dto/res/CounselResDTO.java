package com.symteo.domain.counsel.dto.res;

import lombok.Builder;

public class CounselResDTO {

    // AI 답변
    @Builder
    public record ChatMessage(
            Long userId,
            Long chatRoomId,
            String userRequest,
            String AiResponse
    ){}

    // AI 상담 요약
    @Builder
    public record ChatSummary(
            String chatSummary
    ){}
}
