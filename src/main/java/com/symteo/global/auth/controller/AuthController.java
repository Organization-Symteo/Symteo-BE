package com.symteo.global.auth.controller;

import com.symteo.global.ApiPayload.ApiResponse;
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
    public ApiResponse<AuthResponse> login(
            @PathVariable String provider,
            @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(provider, request.getToken());
        return ApiResponse.onSuccess(response);
    }

    // 2. 토큰 재발급
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.reissue(request.getRefreshToken());
        return ApiResponse.onSuccess(response);
    }

    // 3. 로그아웃
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.onSuccess("로그아웃 되었습니다.");
    }

    // 4. 회원 탈퇴
    @DeleteMapping("/withdraw")
    public ApiResponse<String> withdraw(@RequestBody WithdrawRequest request) {
        authService.withdraw(request.getUserId());
        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }

}
