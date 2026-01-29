package com.symteo.domain.todayMission.dto;

import com.symteo.domain.todayMission.entity.mapping.UserMissions;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionResponse {

    private String contents;
    private long remainingSeconds;
    private boolean isRestarted;

    public static MissionResponse from(UserMissions userMission, long remainingSeconds) {
        return MissionResponse.builder()
                .contents(userMission.getMissions().getMissionContents())
                .remainingSeconds(remainingSeconds)
                .isRestarted(userMission.isRestarted()) // UserMissions의 필드 사용
                .build();
    }
}