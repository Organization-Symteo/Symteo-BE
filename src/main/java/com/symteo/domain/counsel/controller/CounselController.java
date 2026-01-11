package com.symteo.domain.counsel.controller;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.service.CounselQueryService;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counsel")
@RequiredArgsConstructor
public class CounselController {

    private final CounselQueryService counselQueryService;

    @GetMapping("/upload")
    public ApiResponse<CounselResDTO.askAiDTO> askAI(
            CounselReqDTO.askAiDTO dto
    ){
        return ApiResponse.onSuccess(counselQueryService.createCounsel(dto));
    }
}
