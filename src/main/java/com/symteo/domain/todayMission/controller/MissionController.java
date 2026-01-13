package com.symteo.domain.todayMission.controller;

import com.symteo.domain.todayMission.dto.MissionResponse;
import com.symteo.domain.todayMission.dto.UserMissionStartResponse;
import com.symteo.domain.todayMission.service.MissionService;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/missions")
public class MissionController {

    private final MissionService missionService;
    private final UserRepository userRepository;

    // 오늘의 미션 조회
    @GetMapping("/today")
    public ApiResponse<MissionResponse> getTodayMission() {
        return ApiResponse.onSuccess(
                missionService.getTodayMission()
        );
    }


}