package com.symteo.domain.todayMission.service;

import com.symteo.domain.todayMission.dto.MissionResponse;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.todayMission.exception.TodayMissionErrorCode;
import com.symteo.domain.todayMission.repository.UserMissionRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import com.symteo.global.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionQueryService {

    private final UserMissionRepository userMissionRepository;
    private final UserRepository userRepository;

    // 오늘의 미션 조회 api
    public MissionResponse getTodayMission(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        UserMissions userMission = userMissionRepository.findTopByUserOrderByUserMissionIdDesc(user)
                .orElseThrow(() -> new GeneralException(TodayMissionErrorCode._MISSION_NOT_FOUND));

        // 완료되지 않았을 때만 남은 시간 계산, 완료되면 0 반환
        long remainingSeconds = userMission.isCompleted() ? 0 : TimeUtils.getSecondsUntilEndOfDay();

        return MissionResponse.from(userMission, remainingSeconds);
    }
}