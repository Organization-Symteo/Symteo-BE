package com.symteo.domain.counsel.controller;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.service.CounselCommandService;
import com.symteo.domain.counsel.service.CounselQueryService;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/counsel")
@RequiredArgsConstructor
public class CounselController {

    private final CounselCommandService counselCommandService;

    // AI 상담 요청 보내기
    @PostMapping("/request")
    public ApiResponse<CounselResDTO.ChatMessage> askAI(
            @RequestBody CounselReqDTO.ChatMessage dto
    ){
        log.info(">>>> 컨트롤러 진입 성공! DTO: {}", dto);
        return ApiResponse.onSuccess(counselCommandService.askCounsel(dto));
    }

    // AI 상담 종료하기
    @PatchMapping("/save")
    public ApiResponse<CounselResDTO.ChatSummary> summaryAI(
            @RequestBody CounselReqDTO.ChatSummary dto
    ){
        return ApiResponse.onSuccess(counselCommandService.summaryCounsel(dto));
    }
}
