package com.symteo.domain.user.controller;

import com.symteo.domain.auth.dto.AuthResponse;
import com.symteo.domain.user.dto.UpdateNicknameRequest;
import com.symteo.domain.user.dto.UserProfileResponse;
import com.symteo.domain.user.dto.UserSignUpRequest;
import com.symteo.domain.user.service.UserService;
import com.symteo.global.ApiPayload.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ApiResponse<NicknameCheckResponse> checkNickname(@RequestParam String nickname) {
        boolean isDuplicated = userService.checkNicknameDuplication(nickname);
        return ApiResponse.onSuccess(new NicknameCheckResponse(isDuplicated));
    }


    // 회원가입 완료 (닉네임 설정) API
    @PostMapping("/signup")
    public ApiResponse<AuthResponse> completeSignUp(
            @AuthenticationPrincipal Long userId,
            @RequestBody UserSignUpRequest request
    ) {
        log.info("회원가입 완료 요청 - UserId: {}, Nickname: {}", userId, request.getNickname());
        AuthResponse response = userService.completeSignUp(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // MY 심터 프로필 정보 조회 API (프로필 사진, 닉네임)
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getUserProfile(@AuthenticationPrincipal Long userId) {
        UserProfileResponse response = userService.getUserProfile(userId);
        return ApiResponse.onSuccess(response);
    }

    // 닉네임 수정 API
    @PatchMapping("/nickname")
    public ApiResponse<UserProfileResponse> updateNickname(
            @AuthenticationPrincipal Long userId,
            @RequestBody UpdateNicknameRequest request
    ) {
        UserProfileResponse response = userService.updateNickname(userId, request);
        return ApiResponse.onSuccess(response);
    }

    // response DTO
    record NicknameCheckResponse(boolean isDuplicated) {}

}
