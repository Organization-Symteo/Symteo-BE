package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
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
import org.springframework.stereotype.Service;

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
        Long userId = dto.userId();
        String question = dto.text();
        log.info("1번 작업 완료");

        // 2. 채팅방 정보 확인 후 없으면 생성, 있으면 사용
        ChatRoom chatRoom = (dto.chatRoomId() == null)
                ? chatRoomRepository.save(CounselConverter.toChatRoom(userId))
                : chatRoomRepository.findById(dto.chatRoomId())
                .orElseThrow(() -> new CounselException(CounselErrorCode._CHATROOM_NOT_FOUND));
        log.info("2번 작업 완료 chatRoomId = {}", chatRoom.getChatroom_id());

        // 3. 생성된 ChatRoom에 유저 상담 질문을 ChatMessage로 변환 후 저장
        ChatMessage chatMessage = CounselConverter.toChatMessage(question, chatRoom);
        chatMessageRepository.save(chatMessage);
        log.info("3번 작업 완료 chatMessage = {}", chatMessage.getMessage());

        // 4. AI에게 상담 질문 요청
        String answer;
        try {
            log.info("4번 작업 진입");
            /*
            answer = chatClient.prompt()
                    .user(question)
                    .call()
                    .content();
            */
            log.info("AI-1: prompt() 시작");
            var prompt = chatClient.prompt().user(question);

            log.info("AI-2: call() 직전");
            var result = prompt.call();

            log.info("AI-3: content() 직전");
            answer = result.content();
            log.info("AI-4: content() 완료");
        } catch (Exception e) {
            log.error("AI 호출 중 오류 발생: ", e);
            throw new CounselException(CounselErrorCode._AI_SERVER_ERROR);
        }


        // 5. AI에게 받은 답변을 ChatMessage로 변환 후 ChatRoom에 저장
        ChatMessage AIMessage = CounselConverter.toChatMessage(answer, chatRoom);
        AIMessage.setRole(Role.AI);
        chatMessageRepository.save(AIMessage);
        log.info("5번 작업 완료 chatMessage = {}", AIMessage.getMessage());

        // 6. AI에게 받은 답변을 유저에게 반환
        return CounselConverter.EntityToChat(userId, chatRoom.getChatroom_id(), question, answer);
    }

    // OpenAI 상담 요약 메소드
    @Override
    public CounselResDTO.ChatSummary summaryCounsel(CounselReqDTO.ChatSummary dto) {
        return null;
    }
}
