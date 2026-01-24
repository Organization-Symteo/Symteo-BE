package com.symteo.global.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor //DevAuthController 관련 설정
@AllArgsConstructor  //DevAuthController 관련 설정
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private boolean isRegistered; // true: 정회원, false: 아직 닉네임 설정이 필요한 단계
    private Long userId;
    private String nickname;
}
