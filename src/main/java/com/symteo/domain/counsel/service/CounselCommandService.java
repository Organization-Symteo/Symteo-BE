package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;

public interface CounselCommandService {
    public CounselResDTO.ChatMessage askCounsel(Long userId, CounselReqDTO.ChatMessage dto);

    public CounselResDTO.ChatSummary summaryCounsel(Long userId, CounselReqDTO.ChatSummary dto);

    public Long deleteChat(Long userId, Long chatRoomId);
}
