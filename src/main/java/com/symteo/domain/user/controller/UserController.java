package com.symteo.domain.user.controller;

import com.symteo.domain.auth.dto.AuthResponse;
import com.symteo.domain.user.dto.UserSignUpRequest;
import com.symteo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    // 닉네임 중복 확인 API
    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        try {
            boolean isDuplicated = userService.checkNicknameDuplication(nickname);

            // isDuplicated가 true면 중복(사용불가), false면 사용 가능
            return ResponseEntity.ok(new NicknameCheckResponse(isDuplicated));

        } catch (IllegalArgumentException e) {
            // 유효성(글자수, 특수문자 등) 실패 시 400 에러 리턴
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // 회원가입 완료 (닉네임 설정) API
    @PostMapping("/signup")
    public ResponseEntity<?> completeSignUp(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserSignUpRequest request
    ) {
        log.info("회원가입 완료 요청 - UserId: {}, Nickname: {}", userId, request.getNickname());

        try {
            // userId가 null일 경우 - 토큰 오류
            if (userId == null) {
                return ResponseEntity.status(401).body("인증 정보가 유효하지 않습니다.");
            }

            AuthResponse response = userService.completeSignUp(userId, request);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 중복된 닉네임이거나 유효하지 않은 입력일 경우
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 처리 중 에러 발생", e);
            return ResponseEntity.internalServerError().body("서버 에러가 발생했습니다.");
        }
    }

    // response DTO
    record NicknameCheckResponse(boolean isDuplicated) {}

}
