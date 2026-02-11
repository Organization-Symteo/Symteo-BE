package com.symteo.domain.todayMission.controller;

import com.symteo.domain.todayMission.dto.*;
import com.symteo.domain.todayMission.service.MissionQueryService;
import com.symteo.domain.todayMission.service.MissionCommandService;
import com.symteo.global.ApiPayload.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionCommandService missionCommandService;
    private final MissionQueryService missionQueryService;

    // 오늘의 미션 조회
    @GetMapping("/today")
    public ApiResponse<MissionResponse> getTodayMission(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionQueryService.getTodayMission(userId)
        );
    }

    // 오늘의 미션 제출 시작 (이미지 제출 병합)
    @PostMapping(
            value = "/{missionId}/submissions",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<UserMissionStartResponse> startMission(
            @PathVariable Long missionId,
            @AuthenticationPrincipal Long userId,
            @RequestPart(value = "request") @Valid UserMissionStartRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                missionCommandService.startMission(missionId, userId, request.getContents(), image)
        );
    }

    // 오늘의 미션 임시저장
    @PostMapping("/{userMissionId}/drafts")
    public ApiResponse<DraftSaveResponse> saveDraft(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid DraftSaveRequest request
    ) {
        return ApiResponse.onSuccess(
                missionCommandService.saveDraft(userMissionId, userId, request.getContents())
        );
    }

    // 오늘의 미션 완료 처리
    @PatchMapping("/{userMissionId}/status")
    public ApiResponse<UserMissionCompletedResponse> saveCompletedMission(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionCommandService.saveCompletedMission(userMissionId, userId)
        );
    }

    // 오늘 미션 새로고침 API
    @PatchMapping("/today-mission/refresh")
    public ApiResponse<MissionResponse> refreshTodayMission(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionCommandService.refreshTodayMission(userId)
        );
    }
}