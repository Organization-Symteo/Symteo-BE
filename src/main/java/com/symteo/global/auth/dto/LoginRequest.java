package com.symteo.global.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
//소셜 로그인을 통해 발급받은 access token을 앱 토큰(JWT)로 변경
public class LoginRequest {
    private String token; // 프론트에서 주는 소셜 Access Token
}
