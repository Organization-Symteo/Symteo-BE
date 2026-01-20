package com.symteo.domain.counsel.converter;

import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CounselConverter {

    /// 맞춤 상담
    // 엔티티 -> DTO
    // AI 답변을 String으로 변환
    // AI 관련으로 만든 ChatRoom 정보와 요청 답변을 한 set로 전송
    public static CounselResDTO.ChatMessage EntityToChatSet(
            ChatRoom chatRoom,
            String question,
            String answer
    ){
        return CounselResDTO.ChatMessage.builder()
                .userId(chatRoom.getUserId())
                .chatRoomId(chatRoom.getChatroomId())
                .userRequest(question)
                .AiResponse(answer)
                .build();
    }

    // 엔티티 -> DTO
    // 맞춤 상담 - 사용자 입력
    // 사용자가 저장하기를 누르면 전체 채팅 내역을 AI를 통해 요약한 후 반환
    public static CounselResDTO.ChatSummary EntityToChatSummary(
            ChatRoom chatRoom
    ){
        return CounselResDTO.ChatSummary.builder()
                .userId(chatRoom.getUserId())
                .chatRoomId(chatRoom.getChatroomId())
                .chatSummary(chatRoom.getChatSummary())
                .build();
    }

    /// 마이 심터
    // DTO -> DTO
    // 마이 심터 - 상담기록 - 상담목록
    // ChatRoom의 updated_at 시각과 chatSummary 정보를 chat으로 반환
    public static CounselResDTO.Chat ChatRoomToChat(
            ChatRoom chatRoom
    ){
        LocalDateTime dateTime = LocalDateTime.parse(chatRoom.getUpdatedAt().toString());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

        return CounselResDTO.Chat.builder()
                .dateTime(dateTime.format(formatter))
                .chatSummary(chatRoom.getChatSummary())
                .chatRoomId(chatRoom.getChatroomId())
                .build();
    }

    // 엔티티 -> DTO
    // 마이 심터 - 상담기록 - 상담요약
    public static CounselResDTO.ChatSummary EntityToChatDetails(
            ChatRoom chatRoom
    ) {
        return CounselResDTO.ChatSummary.builder()
                .userId(chatRoom.getUserId())
                .chatRoomId(chatRoom.getChatroomId())
                .chatSummary(chatRoom.getChatSummary())
                .userSummary(chatRoom.getUserSummary())
                .aiSummary(chatRoom.getAiSummary())// 주제
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
