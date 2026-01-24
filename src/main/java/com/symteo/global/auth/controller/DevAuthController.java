package com.symteo.global.auth.controller;

import com.symteo.global.auth.dto.AuthResponse;
import com.symteo.domain.user.enums.Role;
import com.symteo.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//swagger 테스트 시에 사용할 임시 토큰 발급을 위한 파일이므로 후에 삭제 예정
@RestController
@RequestMapping("/api/v1/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final JwtProvider jwtProvider;

    // URL: GET /api/v1/dev/login?userId=1
    @GetMapping("/login")
    public ResponseEntity<AuthResponse> devLogin(@RequestParam Long userId) {

        // 1. 토큰 생성
        String accessToken = jwtProvider.createAccessToken(userId, Role.USER);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // 2. 응답 생성 (변경된 DTO 순서에 맞춰서 5개 값을 넣어줍니다)
        // 순서: accessToken, refreshToken, isNewMember, userId, nickname
        AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                false,          // isNewMember: 개발용이니까 그냥 false(기존회원)로 고정
                userId,         // userId
                "Developer"     // nickname: 임의 닉네임
        );

        return ResponseEntity.ok(response);
    }
}
