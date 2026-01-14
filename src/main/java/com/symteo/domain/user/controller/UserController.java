package com.symteo.domain.user.controller;

import com.symteo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // API: 닉네임 중복 확인
    // GET /api/v1/users/check-nickname?nickname=테스트
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        try {
            boolean isDuplicated = userService.checkNicknameDuplication(nickname);

            // isDuplicated가 true면 중복(사용불가), false면 사용가능
            return ResponseEntity.ok(new NicknameCheckResponse(isDuplicated));

        } catch (IllegalArgumentException e) {
            // 유효성(글자수, 특수문자 등) 실패 시 400 에러 리턴
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // response DTO
    record NicknameCheckResponse(boolean isDuplicated) {}
}
