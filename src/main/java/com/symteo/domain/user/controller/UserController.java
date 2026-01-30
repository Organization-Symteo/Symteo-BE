package com.symteo.domain.user.controller;

import com.symteo.global.auth.dto.AuthResponse;
import com.symteo.domain.user.dto.*;
import com.symteo.domain.user.service.UserService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ApiResponse<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        boolean isDuplicated = userService.checkNicknameDuplication(nickname);
        return ApiResponse.onSuccess(new NicknameCheckResponse(isDuplicated));
    }

    // 회원가입 완료 (닉네임 설정) API
    @PostMapping("/signup")
    public ApiResponse<AuthResponse> completeSignUp(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserSignUpRequest request
    ) {
        log.info("회원가입 완료 요청 - UserId: {}, Nickname: {}", userId, request.getNickname());
        AuthResponse response = userService.completeSignUp(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // MY 심터 프로필 정보 조회 API (프로필 사진, 닉네임)
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.onSuccess(response);
    }

    // 닉네임 수정 API
    @PatchMapping("/nickname")
    public ApiResponse<UserProfileResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateNicknameRequest request
    ) {
        UserProfileResponse response = userService.updateNickname(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // 환경설정 토글 상태 및 앱 버전 조회 API
    @GetMapping("/settings")
    public ApiResponse<UserSettingsResponse> getUserSettings(@AuthenticationPrincipal Long userId) {
        UserSettingsResponse response = userService.getUserSettings(userId);
        return ApiResponse.onSuccess(response);
    }

    // 환경설정 업데이트 API
    @PatchMapping("/settings")
    public ApiResponse<UserSettingsResponse> updateUserSettings(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateUserSettingsRequest request
    ) {
        UserSettingsResponse response = userService.updateUserSettings(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // AI 상담사 설정 조회 API
    @GetMapping("/counselor-settings")
    public ApiResponse<CounselorSettingsResponse> getCounselorSettings(
            @AuthenticationPrincipal Long userId
    ) {
        CounselorSettingsResponse response = userService.getCounselorSettings(userId);
        return ApiResponse.onSuccess(response);
    }

    // AI 상담사 설정 수정 API
    @PatchMapping("/counselor-settings")
    public ApiResponse<CounselorSettingsResponse> updateCounselorSettings(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateCounselorSettingsRequest request
    ) {
        CounselorSettingsResponse response = userService.updateCounselorSettings(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // 완료한 미션 리스트 조회 API (MY 심터)
    @GetMapping("/missions/history")
    public ApiResponse<MissionHistoryResponse.MissionListResponse> getCompletedMissions(
            @AuthenticationPrincipal Long userId
    ) {
        MissionHistoryResponse.MissionListResponse response = userService.getCompletedMissions(userId);
        return ApiResponse.onSuccess(response);
    }

    // 특정 미션 상세 조회 API (MY 심터)
    @GetMapping("/missions/history/{userMissionId}")
    public ApiResponse<MissionHistoryResponse.MissionDetailResponse> getMissionDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long userMissionId
    ) {
        MissionHistoryResponse.MissionDetailResponse response = userService.getMissionDetail(userId, userMissionId);
        return ApiResponse.onSuccess(response);
    }

    // 미션 내용 및 이미지 수정 API (MY 심터)
    @PatchMapping("/missions/history/{userMissionId}")
    public ApiResponse<UpdateMissionResponse> updateMission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long userMissionId,
            @RequestPart(required = false) UpdateMissionRequest request,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        UpdateMissionResponse response = userService.updateMission(userId, userMissionId, request, images);
        return ApiResponse.onSuccess(response);
    }

    // response DTO
    record NicknameCheckResponse(boolean isDuplicated) {}

}
