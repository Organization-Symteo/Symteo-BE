package com.symteo.domain.counsel.controller;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.service.CounselCommandService;
import com.symteo.domain.counsel.service.CounselQueryService;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/counsel")
@RequiredArgsConstructor
public class CounselController {

    private final CounselCommandService counselCommandService;

    // AI 상담 요청 보내기
    @PostMapping("/request")
    public ApiResponse<CounselResDTO.ChatMessage> askAI(
            CounselReqDTO.ChatMessage dto
    ){
        return ApiResponse.onSuccess(counselCommandService.askCounsel(dto));
    }

    // AI 상담 종료하기
    @PatchMapping("/save")
    public ApiResponse<CounselResDTO.ChatSummary> summaryAI(
            CounselReqDTO.ChatSummary dto
    ){
        return ApiResponse.onSuccess(counselCommandService.summaryCounsel(dto));
    }
}
