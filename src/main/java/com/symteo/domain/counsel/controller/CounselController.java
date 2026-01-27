package com.symteo.domain.counsel.controller;

import com.symteo.domain.counsel.dto.req.CounselReqDTO;
import com.symteo.domain.counsel.dto.req.CounselorSettingReqDTO;
import com.symteo.domain.counsel.dto.res.CounselResDTO;
import com.symteo.domain.counsel.service.CounselCommandService;
import com.symteo.domain.counsel.service.CounselQueryService;
import com.symteo.domain.counsel.service.CounselorService;

import com.symteo.global.ApiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/counsel")
@RequiredArgsConstructor
public class CounselController {

    private final CounselCommandService counselCommandService;
    private final CounselQueryService counselQueryService;
    private final CounselorService counselorService;

    // AI 상담 요청 보내기
    @PostMapping("/request")
    public ApiResponse<CounselResDTO.ChatMessage> askAI(
            @RequestBody CounselReqDTO.ChatMessage dto
    ){
        return ApiResponse.onSuccess(counselCommandService.askCounsel(dto));
    }

    // AI 상담 종료하기
    @PatchMapping("/save")
    public ApiResponse<CounselResDTO.ChatSummary> summaryAI(
            @RequestBody CounselReqDTO.ChatSummary dto
    ){
        return ApiResponse.onSuccess(counselCommandService.summaryCounsel(dto));
    }

    // 전체 상담 조회하기
    // 나중에 Spring JWT에서 토큰 속 id를 찾자
    @GetMapping("/all")
    public ApiResponse<List<CounselResDTO.Chat>> getALlChat(
            @RequestParam Long userId
    ){
        return ApiResponse.onSuccess(counselQueryService.readAllChat(userId));
    }

    // 단일 상담 조회하기
    @GetMapping("/{counselId}")
    public ApiResponse<CounselResDTO.ChatSummary> getChat(
            @PathVariable Long counselId
    ){
        return ApiResponse.onSuccess(counselQueryService.readChat(counselId));
    }

    // 상담 삭제하기
    @DeleteMapping("/{counselId}")
    public ApiResponse<Long> deleteChat(
            @PathVariable Long counselId
    ){
        return ApiResponse.onSuccess(counselCommandService.deleteChat(counselId));
    }

    // 상담사 초기 설정 저장
    @PostMapping("/settings")
    public ResponseEntity<String> saveSettings(@RequestBody CounselorSettingReqDTO request) {

        counselorService.saveSettings(request);

        return ResponseEntity.ok("상담사 초기 설정이 저장되었습니다.");
    }

}
