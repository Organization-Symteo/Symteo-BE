package com.symteo.domain.todayMission.controller;

import com.symteo.domain.todayMission.dto.*;
import com.symteo.domain.todayMission.service.MissionService;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionService missionService;
    private final UserRepository userRepository;

    // 오늘의 미션 조회
    @GetMapping("/today")
    public ApiResponse<MissionResponse> getTodayMission(
            @AuthenticationPrincipal Long userId
    ) {
        return ApiResponse.onSuccess(
                missionService.getTodayMission(userId)
        );
    }

    // 오늘의 미션 제출 시작
    @PostMapping("/{missionId}/start")
    public ApiResponse<UserMissionStartResponse> startMission(
            @PathVariable Long missionId,
            @AuthenticationPrincipal Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        return ApiResponse.onSuccess(
                missionService.startMission(missionId, user)
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

    // 오늘의 미션 이미지 추가
    @PostMapping("/{userMissionId}/image")
    public ApiResponse<ImageSaveResponse> saveimage(
            @PathVariable Long userMissionId,
            @AuthenticationPrincipal Long userId,
            @RequestBody ImageSaveRequest request
    ) {
    return ApiResponse.onSuccess(
            missionService.saveImage(userMissionId, userId, request.getImageUrl())
        );
    }
}
