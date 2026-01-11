package com.symteo.domain.auth.service;

import com.symteo.domain.auth.dto.AuthResponse;
import com.symteo.domain.auth.repository.UserTokenRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.entity.UserTokens;
import com.symteo.domain.user.repository.UserRepository;
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
        // 1. 소셜 서버에서 사용자 정보(식별자) 가져오기
        SocialUserInfo socialUser = socialLoadStrategy.getSocialInfo(provider, accessToken);

        // 2. DB 조회 (없으면 회원가입, 있으면 로그인)
        User user = userRepository.findBySocialTypeAndSocialId(socialUser.getSocialType(), socialUser.getSocialId())
                .orElseGet(() -> registerUser(socialUser));

        // 3. 앱 전용 토큰(JWT) 발급
        String appAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String appRefreshToken = jwtProvider.createRefreshToken(user.getId());

        // 4. Refresh Token 저장
        saveRefreshToken(user, appRefreshToken);

        // 5. 응답 생성
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
        // 만료 시간은 보통 2주 (14일)
        LocalDateTime expiresAt = LocalDateTime.now().plusWeeks(2);

        UserTokens token = UserTokens.builder()
                .user(user)
                .refreshToken(refreshToken)
                .expiresAt(expiresAt)
                .build();
        userTokenRepository.save(token);
    }
}
