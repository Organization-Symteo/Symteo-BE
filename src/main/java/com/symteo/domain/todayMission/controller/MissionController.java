package com.symteo.domain.todayMission.controller;

import com.symteo.domain.todayMission.dto.*;
import com.symteo.domain.todayMission.service.MissionService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionService missionService;

    // 오늘의 미션 조회
    @GetMapping("/today")
    public ApiResponse<MissionResponse> getTodayMission(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionService.getTodayMission(userId)
        );
    }

    // 오늘의 미션 제출 시작 (이미지 제출 병합)
    @PostMapping(
            value = "/{missionId}/start",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<UserMissionStartResponse> startMission(
            @PathVariable Long missionId,
            @AuthenticationPrincipal Long userId,
            @RequestPart(required = false) String contents,
            @RequestPart(required = false) MultipartFile image
    ) {
        return ApiResponse.onSuccess(
                missionService.startMission(missionId, userId, contents, image)
        );
    }

    // 오늘의 미션 임시저장
    @PostMapping("/{userMissionId}/draft")
    public ApiResponse<DraftSaveResponse> saveDraft(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal Long userId,
            @RequestBody DraftSaveRequest request
    ) {
        return ApiResponse.onSuccess(
                missionService.saveDraft(userMissionId, userId, request.getContents())
        );
    }

    // 오늘의 미션 완료 처리
    @PostMapping("/{userMissionId}/completed")
    public ApiResponse<UserMissionCompletedResponse> saveCompletedMission(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionService.saveCompletedMission(userMissionId, userId)
        );
    }

}