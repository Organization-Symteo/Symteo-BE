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
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CounselCommandServiceImpl implements CounselCommandService{

    private final ChatClient chatClient;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // OpenAI 요청 메소드
    @Override
    public CounselResDTO.ChatMessage askCounsel(CounselReqDTO.ChatMessage dto){
        // 1. DTO에서 상담 정보 추출
        Long userId = dto.userId();
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
        String answer =  chatClient.prompt()
                .user(question)
                .call()
                .content();

        // 5. AI에게 받은 답변을 ChatMessage로 변환 후 ChatRoom에 저장
        ChatMessage AIMessage = CounselConverter.toChatMessage(answer, chatRoom);
        AIMessage.setRole(Role.AI);
        chatMessageRepository.save(AIMessage);

        // 6. AI에게 받은 답변을 유저에게 반환
        return CounselConverter.EntityToChat(userId, chatRoom.getChatroom_id(), question, answer);
    }

    // OpenAI 상담 요약 메소드
    @Override
    public CounselResDTO.ChatSummary summaryCounsel(CounselReqDTO.ChatSummary dto) {
        
        return null;
    }
}
