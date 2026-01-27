package com.symteo.domain.todayMission.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMissionCompletedResponse {
    private Long userMissionId;
    private boolean isCompleted;
    private Long remainingSeconds;
}