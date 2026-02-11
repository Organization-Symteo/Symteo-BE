package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// @Service
@RequiredArgsConstructor
public class CounselCommandRedisServiceImpl implements CounselCommandService{
    @Override
    public CounselResDTO.ChatMessage askCounsel(Long userId, CounselReqDTO.ChatMessage dto) {
        // 1) Redis - 이전 상담 내역 조회

        // 2) Redis - 사용자 상담 설정 로딩
        // 3) 프롬프트 로딩
        // 4) AI 호출
        // 5) 사용자 질문 + 답변 저장
        return null;
    }

    @Override
    public CounselResDTO.ChatMessage askReport(Long userId, CounselReqDTO.ChatReport dto) {
        return null;
    }

    @Override
    public CounselResDTO.ChatSummary summaryCounsel(Long userId, Long counselId) {
        return null;
    }

    @Override
    public Long deleteChat(Long userId, Long chatRoomId) {
        return 0L;
    }
}
