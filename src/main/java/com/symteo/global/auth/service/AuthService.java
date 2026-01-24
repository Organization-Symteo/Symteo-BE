package com.symteo.global.auth.service;

import com.symteo.global.auth.dto.AuthResponse;
import com.symteo.global.auth.repository.UserTokenRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.entity.UserTokens;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import com.symteo.global.jwt.JwtProvider;
import com.symteo.global.oauth.info.SocialUserInfo;
import com.symteo.global.oauth.service.SocialLoadStrategy;
import org.springframework.transaction.annotation.Transactional;
import com.symteo.domain.user.enums.Role;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SocialLoadStrategy socialLoadStrategy;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtProvider jwtProvider;

    @Transactional
    public AuthResponse login(String provider, String accessToken) {
        // 1. DB에서 사용자 정보(식별자) 가져오기
        SocialUserInfo socialUser = socialLoadStrategy.getSocialInfo(provider, accessToken);

        // 2. DB에서 유저 조회
        // 회원 탈퇴한 유저도 조회 가능
        User user = userRepository.findBySocialTypeAndSocialId(socialUser.getSocialType(), socialUser.getSocialId())
                .orElse(null);

        // 3. 회원가입 or 로그인
        // CASE 1: 아예 없는 유저라면 -> 회원가입 진행
        if (user == null) {
            user = registerUser(socialUser);
        }

        // CASE 2: 탈퇴 이력이 있는 유저 (Soft Delete 상태)
        else if (user.getDeletedAt() != null) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            // A. 탈퇴한 지 7일이 아직 안 지남 -> 재가입 불가 (에러)
            if (user.getDeletedAt().isAfter(sevenDaysAgo)) {
                throw new GeneralException(ErrorStatus._WITHDRAWAL_RESTRICTION);
            }

            // B. 탈퇴한 지 7일이 지남 -> 완전 삭제 후 신규 가입 처리
            userRepository.delete(user);
            userRepository.flush();
            user = registerUser(socialUser);
        }

        // 4. 앱 전용 토큰(JWT) 발급
        String appAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String appRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 5. Refresh Token 저장
        saveRefreshToken(user, appRefreshToken);

        // 6. 응답 생성
        return AuthResponse.builder()
                .accessToken(appAccessToken)
                .refreshToken(appRefreshToken)
                .isRegistered(user.getRole() == Role.USER) // USER면 가입완료, GUEST면 미완료
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    // 신규 유저 저장 (GUEST 권한)
    private User registerUser(SocialUserInfo socialUser) {
        User newUser = User.builder()
                .socialType(socialUser.getSocialType())
                .socialId(socialUser.getSocialId())
                .role(Role.GUEST) // 초기엔 GUEST
                .build();
        return userRepository.save(newUser);
    }

    // 리프레시 토큰 저장
    private void saveRefreshToken(User user, String refreshToken) {
        // 만료 시간은 2주 (14일)
        LocalDateTime expiresAt = LocalDateTime.now().plusWeeks(2);

        UserTokens token = UserTokens.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
        userTokenRepository.save(token);
    }

    @Transactional
    public AuthResponse reissue(String refreshToken) {

        // 1. 넘어온 Refresh Token이 유효한지 검사 (위조 여부 확인)
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다. 다시 로그인해주세요.");
        }

        // 2. DB에서 해당 Refresh Token을 가진 정보 확인
        UserTokens userTokens = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("토큰 정보를 찾을 수 없습니다."));

        // 3. 토큰 주인(User) 정보 가져오기
        User user = userTokens.getUser();

        // 4. 새로운 토큰 쌍 발급 (Access + Refresh)
        String newAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 5. DB 정보 업데이트 (Dirty Checking)
        userTokens.updateRefreshToken(newRefreshToken, LocalDateTime.now().plusWeeks(2));

        // 6. 새로운 토큰 세트 반환
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .isRegistered(user.getRole() == Role.USER) // 가입 완료 여부
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        UserTokens token = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorStatus._TOKEN_NOT_FOUND));

        userTokenRepository.delete(token);
    }

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }

        user.deleteSoftly();

        userTokenRepository.deleteAllByUserId(userId);
    }
}
