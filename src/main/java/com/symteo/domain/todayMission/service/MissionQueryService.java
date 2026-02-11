package com.symteo.domain.todayMission.service;

import com.symteo.domain.todayMission.dto.MissionResponse;
import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import com.symteo.domain.todayMission.exception.TodayMissionErrorCode;
import com.symteo.domain.todayMission.repository.UserMissionRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

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

        LocalDateTime endOfToday = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        long remainingSeconds = 0;

        if (!userMission.isCompleted()) {
            remainingSeconds = Math.max(
                    Duration.between(LocalDateTime.now(), endOfToday).getSeconds(), 0);
        }
        return MissionResponse.from(userMission, remainingSeconds);
    }
}