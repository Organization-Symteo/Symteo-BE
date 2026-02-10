package com.symteo.domain.counsel.dto.req;

import com.symteo.domain.diagnose.enums.DiagnoseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CounselReqDTO {

    // 사용자가 보낸 AI 상담 정보
    // 1. 유저 아이디, 2. 채팅방 정보, 3. 채팅 질문
    public record ChatMessage(
            Long chatRoomId, // null 이어야지 새로운 채팅방 개설
            @NotBlank(message = "메시지 내용은 필수입니다.") String text
    ){}

    // 사용자의 AI 상담 요약 요청
    // '종료하기' 누를 때 AI에게 ChatRoom의 전체 채팅 내역을 보낼 때 사용
    public record ChatSummary(
            @NotNull(message = "채팅방 ID는 필수입니다.") Long chatRoomId
    ){}

    // 상담 중 리포트 불러오기
    public record ChatReport(
            @NotNull(message = "채팅방 ID는 필수입니다.") Long chatRoomId,
            @NotNull(message = "리포트 타입은 필수입니다.") DiagnoseType reportType,
            @NotNull(message = "리포트 ID는 필수입니다.") Long reportId
    ){}
}
