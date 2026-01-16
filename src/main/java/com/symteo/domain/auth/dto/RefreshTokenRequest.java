package com.symteo.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
//JWT 만료 시에 refresh Token을 발급받음(토큰 재발급)
public class RefreshTokenRequest {
    private String refreshToken;
}
