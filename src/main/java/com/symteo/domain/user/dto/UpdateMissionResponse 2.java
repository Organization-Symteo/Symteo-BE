package com.symteo.domain.user.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UpdateMissionResponse(
        Long userMissionId,
        String draftContents,
        List<String> imageUrls
) {
}

