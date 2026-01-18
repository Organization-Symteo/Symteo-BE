package com.symteo.domain.counsel.dto.req;

public class CounselReqDTO {

    // 사용자가 보낸 AI 상담 정보
    // 1. 유저 아이디, 2. 채팅방 정보, 3. 채팅 질문
    public record ChatMessage(
            Long userId,
            Long chatRoomId,
            String text
    ){}

    // 사용자의 AI 상담 요약 요청
    public record ChatSummary(
            Long userId,
            Long chatRoomId
    ){}
}
