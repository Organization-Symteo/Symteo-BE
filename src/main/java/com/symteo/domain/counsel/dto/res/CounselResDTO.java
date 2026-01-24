package com.symteo.domain.counsel.dto.res;

import lombok.Builder;

import java.time.LocalDateTime;

public class CounselResDTO {

    // AI 답변
    // 사용자 요청 시 응답과 같이 반환
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
            Long userId,
            Long chatRoomId,
            String chatSummary,
            String userSummary,
            String aiSummary
    ){}

    // 채팅 전달
    // List<Chat>
    // 채팅의 요약본 전체를 전달할 때 사용되는 하나의 DTO 형태
    @Builder
    public record Chat(
            String dateTime,
            String chatSummary,
            Long counselId
    ){}

}
