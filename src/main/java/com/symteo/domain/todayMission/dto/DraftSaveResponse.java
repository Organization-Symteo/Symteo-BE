package com.symteo.domain.todayMission.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DraftSaveResponse {
    private Long draftId;
    private boolean isDrafted;
    private LocalDateTime updatedAt;
}
