package com.symteo.global.auth.controller;

import com.symteo.global.auth.dto.*;
import com.symteo.global.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // 1. 소셜 로그인
    @PostMapping("/login/{provider}")
    public ResponseEntity<AuthResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request
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

    // 3. 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());

        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    // 4. 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        authService.withdraw(request.getUserId());

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

}
