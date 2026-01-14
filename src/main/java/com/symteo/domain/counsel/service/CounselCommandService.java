package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;

public interface CounselCommandService {
    public CounselResDTO.ChatMessage createCounsel(CounselReqDTO.ChatMessage dto);
}
