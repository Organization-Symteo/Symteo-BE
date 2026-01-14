package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.repository.CounselRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CounselCommandServiceImpl implements CounselCommandService{

    private final ChatClient chatClient;
    private final CounselRepository counselRepository;

    public CounselCommandServiceImpl(ChatClient.Builder builder, CounselRepository counselRepository){
        this.chatClient = builder.build();
        this.counselRepository = counselRepository;
    }

    // OpenAI 요청 메소드
    @Override
    public CounselResDTO.ChatMessage createCounsel(CounselReqDTO.ChatMessage dto){
        // 1. DTO에서 사용자 상담 질문 반환
        String question = dto.text();

        // 2. 유저 정보를 이용해 ChatRoom 생성, 없으면 만들기

        // 3. 생성된 ChatRoom에 유저 상담 질문을 ChatMessage로 변환 후 저장

        // 4. AI에게 상담 질문 요청
        String answer =  chatClient.prompt()
                .user(question)
                .call()
                .content();

        // 5. AI에게 받은 답변을 ChatMessage로 변환 후 ChayRoom에 저장

        // 6. AI에게 받은 답변을 유저에게 반환
        return CounselConverter.EntityToChatMessage(answer);
    }
}
