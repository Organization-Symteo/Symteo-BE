package com.symteo.domain.counsel.service;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;

public interface CounselQueryService {
    public CounselResDTO.askAiDTO createCounsel(CounselReqDTO.askAiDTO dto);
}
