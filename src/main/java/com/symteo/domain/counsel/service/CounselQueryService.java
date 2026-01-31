package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.entity.ChatRoom;
import com.symteo.global.ApiPayload.ApiResponse;

import java.util.List;

public interface CounselQueryService {
    public List<CounselResDTO.Chat> readAllChat(Long userId);
    public CounselResDTO.ChatSummary readChat(Long userId, Long chatRoomId);
}
