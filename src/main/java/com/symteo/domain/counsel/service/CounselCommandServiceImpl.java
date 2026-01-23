package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
import com.symteo.domain.counsel.dto.AISummaryDTO;
import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatMessage;
import com.symteo.domain.counsel.entity.ChatRoom;
import com.symteo.domain.counsel.enums.Role;
import com.symteo.domain.counsel.exception.code.CounselErrorCode;
import com.symteo.domain.counsel.exception.code.CounselException;
import com.symteo.domain.counsel.repository.ChatMessageRepository;
import com.symteo.domain.counsel.repository.ChatRoomRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CounselCommandServiceImpl implements CounselCommandService{

    private final ChatClient chatClient;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // OpenAI 요청 메소드
    @Override
    @Transactional
    public CounselResDTO.ChatMessage askCounsel(CounselReqDTO.ChatMessage dto) {
        // 1. DTO에서 상담 정보 추출
        Long userId = dto.userId(); // JWT 변경 해야됨
        String question = dto.text();

        // 2. 채팅방 정보 확인 후 없으면 생성, 있으면 사용
        ChatRoom chatRoom = (dto.chatRoomId() == null)
                ? chatRoomRepository.save(CounselConverter.toChatRoom(userId))
                : chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        // 3. 생성된 ChatRoom에 유저 상담 질문을 ChatMessage로 변환 후 저장
        ChatMessage chatMessage = CounselConverter.toChatMessage(question, chatRoom);
        chatMessageRepository.save(chatMessage);

        // 4. AI에게 상담 질문 요청
        String answer;
        try {
            answer = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI 호출 중 오류 발생: ", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }

        // 5. AI에게 받은 답변을 ChatMessage로 변환 후 ChatRoom에 저장
        ChatMessage AIMessage = CounselConverter.toChatMessage(answer, chatRoom);
        AIMessage.setRole(Role.AI);
        chatMessageRepository.save(AIMessage);

        // 6. AI에게 받은 답변을 유저에게 반환
        return CounselConverter.EntityToChatSet(chatRoom, question, answer);
    }

    // OpenAI 상담 종료 및 요약 메소드
    // 전체 채팅, AI 채팅, 유저 채팅을 각각 요약한다.
    @Transactional
    @Override
    public CounselResDTO.ChatSummary summaryCounsel(CounselReqDTO.ChatSummary dto) {
        // 1. 채팅방 찾기
        ChatRoom chatRoom = chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));

        // 2. 채팅 내역 가져오기
        List<ChatMessage> chatMessages = chatRoom.getChatMessages();
        List<ChatMessage> aiMessages = chatMessageRepository.findAllByChatRoom_ChatroomIdAndRole(dto.chatRoomId(), Role.AI);
        List<ChatMessage> userMessages = chatMessageRepository.findAllByChatRoom_ChatroomIdAndRole(dto.chatRoomId(), Role.USER);

        // 2-1. 컨버터 설정 -> AI 답변 반환
        var converter = new BeanOutputConverter<>(AISummaryDTO.ChatMessage.class);

        // 프롬프트 설정
        String promptText = """
            당신은 전문 심리 상담 요약가입니다. 아래 제공된 세 가지 메시지 그룹을 각각 분석하여 요약해주세요.
            [지시 사항]
            1. 모든 요약은 15자 이내의 짧은 문장으로 작성하세요.
            2. 반드시 '명사'로 끝나도록 작성하세요. (예: "심리적 불안감 호소", "휴식 및 상담 권고")
            3. JSON의 모든 필드(chatMessages, userMessages, aiMessages)를 반드시 채워주세요.
               \s
            [데이터]
            - 전체 대화: {chatMessages}
            - 사용자 메시지: {userMessages}
            - AI 메시지: {aiMessages}
           \s""";

        AISummaryDTO.ChatMessage summary = null;
        try {
            summary = chatClient.prompt()
                    .user(u -> u.text(promptText)
                            .param("chatMessages", formatMessages(chatMessages))
                            .param("userMessages", formatMessages(userMessages))
                            .param("aiMessages", formatMessages(aiMessages))
            } catch (Exception e) {
            log.error("e: ", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }

        // 4. AI 요약 내용 ChatRooms에 저장하기
        chatRoom.setChatSummary(Objects.requireNonNull(summary).chatMessages());
        chatRoom.setUserSummary(summary.userMessages());
        chatRoom.setAiSummary(summary.aiMessages());

        // 5. 반환
        return CounselConverter.EntityToChatSummary(chatRoom);
    }

    // 사용자 요청 전처리 메소드
    private String formatMessages(List<ChatMessage> messages) {
        return messages.stream()
                .map(m -> String.format("[%s]: %s", m.getRole(), m.getMessage()))
                .collect(Collectors.joining("\n"));
    }
}
