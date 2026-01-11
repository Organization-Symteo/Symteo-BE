package com.symteo.domain.counsel.controller;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/counsel")
@RequiredArgsConstructor
public class CounselController {

    @GetMapping("")
    public ApiResponse<CounselResDTO.AskDTO> askAI(
            CounselReqDTO.AskDTO dto
    ){
        return null; //ApiResponse.onSuccess(SuccessStatus._OK.getCode(), SuccessStatus._OK.getMessage(), );
    }
}
