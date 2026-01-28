package com.symteo.domain.todayMission.controller;

import com.symteo.domain.todayMission.dto.*;
import com.symteo.domain.todayMission.service.MissionService;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
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

    // 오늘 미션 자동 생성 테스트 api
    @Operation(summary = "미션 수동 생성 테스트 API", description = "호출 시 해당 유저의 미션을 즉시 생성합니다.")
    @GetMapping("/test/generate")
    public ApiResponse<String> testGenerateMission(@AuthenticationPrincipal Long userId) {
        // 1. 현재 로그인한 유저 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 2. 미션 생성 로직 강제 호출
        missionService.generateMissionForUser(user);

        return ApiResponse.onSuccess("미션 생성 성공! DB의 user_missions 테이블을 확인하세요.");
    }

}