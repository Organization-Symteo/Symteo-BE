package com.symteo.domain.todayMission.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMissionStartResponse {
    private long userMissionId;
    private boolean isDrafted;
    private boolean isCompleted;
    private long remainingSeconds;
}