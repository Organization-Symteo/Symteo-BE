package com.symteo.global.auth.dto;

import lombok.Builder;

@Builder
public record AuthResponse (
    String accessToken,
    String refreshToken,
    boolean isRegistered, // true: 정회원, false: 아직 닉네임 설정이 필요한 단계
    Long userId,
    String nickname
){
    public static AuthResponse of(String accessToken, String refreshToken, boolean isRegistered, Long userId, String nickname){
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isRegistered(isRegistered)
                .userId(userId)
                .nickname(nickname)
                .build();
    }
}



