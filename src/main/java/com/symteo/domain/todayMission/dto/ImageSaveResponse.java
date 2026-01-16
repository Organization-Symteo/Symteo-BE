package com.symteo.domain.todayMission.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ImageSaveResponse {
    private Long userMissionId;
    private String imageUrl;
}