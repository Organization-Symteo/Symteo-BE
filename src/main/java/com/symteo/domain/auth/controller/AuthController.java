package com.symteo.domain.auth.controller;

import com.symteo.domain.auth.dto.AuthResponse;
import com.symteo.domain.auth.dto.LoginRequest;
import com.symteo.domain.auth.dto.RefreshTokenRequest;
import com.symteo.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 1. 소셜 로그인 (가입/로그인 통합)
    // URL: POST /api/v1/auth/login/kakao (또는 naver, google)
    @PostMapping("/login/{provider}")
    public ResponseEntity<AuthResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request // { "token": "소셜_액세스_토큰" }
    ) {
        AuthResponse response = authService.login(provider, request.getToken());
        return ResponseEntity.ok(response);
    }

    // 2. 토큰 재발급
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.reissue(request.getRefreshToken());

        return ResponseEntity.ok(response);
    }
    /*
    // 3. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String accessToken) {
        // AuthService에 logout 로직 추가 필요
        return ResponseEntity.ok().build();
    }
    */
}
