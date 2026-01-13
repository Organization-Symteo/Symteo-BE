package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.converter.CounselConverter;
import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class CounselQueryServiceImpl implements CounselQueryService{
    public final ChatClient chatClient;

    public CounselQueryServiceImpl(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }

    // OpenAI 요청 메소드
    @Override
    public CounselResDTO.askAiDTO createCounsel(CounselReqDTO.askAiDTO dto){
        String question = dto.text();

        String answer =  chatClient.prompt()
                .user(question)
                .call()
                .content();

        return CounselConverter.EntityToCounsel(answer);
    }
}
