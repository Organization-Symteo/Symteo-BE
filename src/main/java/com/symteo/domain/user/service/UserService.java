package com.symteo.domain.user.service;
import com.symteo.domain.auth.dto.AuthResponse;
import com.symteo.domain.auth.repository.UserTokenRepository;
import com.symteo.domain.user.dto.UpdateNicknameRequest;
import com.symteo.domain.user.dto.UpdateUserSettingsRequest;
import com.symteo.domain.user.dto.UserProfileResponse;
import com.symteo.domain.user.dto.UserSettingsResponse;
import com.symteo.domain.user.dto.UserSignUpRequest;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.entity.UserSettings;
import com.symteo.domain.user.entity.UserTokens;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.domain.user.repository.UserSettingsRepository;

import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import com.symteo.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final JwtProvider jwtProvider;

    @Value("${app.version:0.0.1}")
    private String appVersion;

    // 닉네임 규칙: 한글/영문/숫자 포함, 3~10자, 특수문자/공백 금지
    private static final String NICKNAME_REGEX = "^[가-힣a-zA-Z0-9]{3,10}$";
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);

    public boolean checkNicknameDuplication(String nickname) {
        // 1. 빈 값 체크(사용자가 값을 입력을 하지 않은 경우)
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new GeneralException(ErrorStatus._NICKNAME_EMPTY);
        }

        // 2. 유효성 검사 (정규식)
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new GeneralException(ErrorStatus._NICKNAME_INVALID);
        }

        // 3. 중복 검사 (DB에 이미 저장된 닉네임인 경우)
        return userRepository.existsByNickname(nickname);
    }

    // 회원가입 완료(닉네임 등록 + Role 변경 + 새 토큰 발급)
    @Transactional
    public AuthResponse completeSignUp(Long userId, UserSignUpRequest request) {

        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 2. 닉네임 중복 재검사 (안전장치)
        if (checkNicknameDuplication(request.getNickname())) {
            throw new GeneralException(ErrorStatus._NICKNAME_DUPLICATED);
        }

        // 3. 닉네임 업데이트 및 권한 승격 (GUEST -> USER)
        user.authorizeUser(request.getNickname());

        // 4. 새로운 토큰 발급 (변경된 Role 정보 포함)
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 5. Refresh Token 교체 (기존 것 삭제하지 않고 업데이트)
        updateRefreshToken(user, newRefreshToken);

        // 6. 응답 반환
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .isRegistered(true)
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    private void updateRefreshToken(User user, String refreshToken) {
        // 기존 토큰 삭제 (User 객체에서 ID를 꺼내서 넘김)
        userTokenRepository.deleteByUserId(user.getId());

        // 새로운 토큰 생성
        UserTokens newToken = UserTokens.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(LocalDateTime.now().plusWeeks(2))
                .build();

        // 저장
        userTokenRepository.save(newToken);
    }

    // MY 심터 프로필 정보 조회 (프로필 사진, 닉네임)
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 프로필 이미지는 현재 null (추후 구현)
        return UserProfileResponse.of(user.getNickname(), null);
    }

    // 닉네임 수정
    @Transactional
    public UserProfileResponse updateNickname(Long userId, UpdateNicknameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // 닉네임 중복 검사
        if (checkNicknameDuplication(request.getNickname())) {
            throw new GeneralException(ErrorStatus._NICKNAME_DUPLICATED);
        }

        // 닉네임 업데이트
        user.authorizeUser(request.getNickname());

        return UserProfileResponse.of(user.getNickname(), null);
    }

    // 환경설정 토글 상태 및 앱 버전 조회
    public UserSettingsResponse getUserSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        // UserSettings 조회 (없으면 기본값으로 생성)
        UserSettings settings = userSettingsRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSettings(user));

        return UserSettingsResponse.of(
                settings.getIsCheerMsgOn(),
                settings.getIsAnalysisReadyOn(),
                settings.getIsMonthlyReportOn(),
                settings.getIsLockEnabled(),
                appVersion
        );
    }

    // 환경설정 업데이트
    @Transactional
    public UserSettingsResponse updateUserSettings(Long userId, UpdateUserSettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        UserSettings settings = userSettingsRepository.findByUser_Id(userId)
                .orElseGet(() -> createDefaultSettings(user));

        // null이 아닌 값만 업데이트
        if (request.getIsCheerMsgOn() != null) {
            settings.updateCheerMsg(request.getIsCheerMsgOn());
        }
        if (request.getIsAnalysisReadyOn() != null) {
            settings.updateAnalysisReady(request.getIsAnalysisReadyOn());
        }
        if (request.getIsMonthlyReportOn() != null) {
            settings.updateMonthlyReport(request.getIsMonthlyReportOn());
        }
        if (request.getIsLockEnabled() != null) {
            settings.updateLockEnabled(request.getIsLockEnabled());
        }

        return UserSettingsResponse.of(
                settings.getIsCheerMsgOn(),
                settings.getIsAnalysisReadyOn(),
                settings.getIsMonthlyReportOn(),
                settings.getIsLockEnabled(),
                appVersion
        );
    }

    // 기본 UserSettings 생성
    private UserSettings createDefaultSettings(User user) {
        UserSettings settings = UserSettings.builder()
                .user(user)
                .build();
        return userSettingsRepository.save(settings);
    }
}

