package com.symteo.domain.user.dto;

import lombok.Builder;

@Builder
public record UserProfileResponse(
    String nickname,
    String profileImageUrl
) {
    public static UserProfileResponse of(String nickname, String profileImageUrl) {
        return UserProfileResponse.builder()
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
