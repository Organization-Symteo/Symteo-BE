package com.symteo.domain.todayMission.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MissionResponse {

    private String contents;
    private long remainingSeconds;
    private boolean isRestarted;
}