package com.symteo.domain.todayMission.dto;

import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionResponse {

    private Long missionId;      // 원본 미션 ID
    private Long userMissionId;  // 유저에게 할당된 미션 기록 ID
    private String contents;
    private long remainingSeconds;
    private boolean isRestarted;
    private boolean isCompleted;

    public static MissionResponse from(UserMissions userMission, long remainingSeconds) {
        return MissionResponse.builder()
                .missionId(userMission.getMissions().getMissionId()) // ID 주입
                .userMissionId(userMission.getUserMissionId())      // 할당 ID 주입
                .contents(userMission.getMissions().getMissionContents())
                .remainingSeconds(remainingSeconds)
                .isCompleted(userMission.isCompleted())
                .isRestarted(userMission.isRestarted())
                .build();
    }
}