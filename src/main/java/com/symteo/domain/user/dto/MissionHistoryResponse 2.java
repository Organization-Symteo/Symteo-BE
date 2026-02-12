package com.symteo.domain.user.dto;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class MissionHistoryResponse {

    // 미션 리스트
    @Builder
    public record MissionListResponse(
        List<MissionItem> missions,
        int totalCount
    ) {
        public static MissionListResponse of(List<MissionItem> missions) {
            return MissionListResponse.builder()
                    .missions(missions)
                    .totalCount(missions.size())
                    .build();
        }
    }

    // 내가 했던 미션 목록
    @Builder
    public record MissionItem(
        Long userMissionId,
        String missionContents,
        LocalDateTime completedAt,
        boolean hasImage
    ) {}

    // 내가 했던 미션 상세 목록
    @Builder
    public record MissionDetailResponse(
        Long userMissionId,
        String missionContents,
        String draftContents,
        List<String> imageUrls,
        LocalDateTime completedAt,
        boolean isCompleted
    ) {}
}

