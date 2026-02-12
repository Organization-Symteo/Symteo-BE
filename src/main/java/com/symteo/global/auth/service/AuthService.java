package com.symteo.global.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.symteo.global.auth.dto.AuthResponse;
import com.symteo.global.auth.repository.UserTokenRepository;
import com.symteo.domain.user.entity.User;
import com.symteo.domain.user.entity.UserTokens;
import com.symteo.domain.user.repository.UserRepository;
import com.symteo.global.ApiPayload.exception.GeneralException;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import com.symteo.global.jwt.JwtProvider;
import com.symteo.global.auth.oauth.info.SocialUserInfo;
import com.symteo.global.auth.oauth.service.SocialLoadStrategy;
/*import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;*/
import org.springframework.transaction.annotation.Transactional;
import com.symteo.domain.user.enums.Role;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
/*import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;*/
import org.springframework.web.client.RestTemplate;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SocialLoadStrategy socialLoadStrategy;
    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final JwtProvider jwtProvider;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

 /*   @Value("${spring.oauth.google.client-id}") private String googleClientId;
    @Value("${spring.oauth.google.client-secret}") private String googleClientSecret;
    @Value("${spring.oauth.google.redirect-uri}") private String googleRedirectUri;
    @Value("${spring.oauth.kakao.client-id}") private String kakaoClientId;
    @Value("${spring.oauth.kakao.redirect-uri}") private String kakaoRedirectUri;
    @Value("${spring.oauth.kakao.client-secret}") private String kakaoClientSecret;
    @Value("${spring.oauth.naver.client-id}") private String naverClientId;
    @Value("${spring.oauth.naver.client-secret}") private String naverClientSecret;
    @Value("${spring.oauth.naver.redirect-uri}") private String naverRedirectUri;

    @Transactional
    public AuthResponse login(String provider, String authCode) {
        // 1. 인가 코드로 소셜 토큰 받아오기
        String socialAccessToken = getSocialAccessToken(provider, authCode);

        // 2. 소셜 토큰으로 유저 정보 가져오기
        SocialUserInfo socialUser = socialLoadStrategy.getSocialInfo(provider, socialAccessToken);

        // 3. 로그인/회원가입 처리
        return processLogin(socialUser);
    }

    private AuthResponse processLogin(SocialUserInfo socialUser) {
        User user = userRepository.findBySocialTypeAndSocialId(socialUser.getSocialType(), socialUser.getSocialId())
                .orElse(null);

        // 신규 가입
        if (user == null) {
            user = registerUser(socialUser);
        }
        // 탈퇴한 유저 재가입 체크
        else if (user.getDeletedAt() != null) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            if (user.getDeletedAt().isAfter(sevenDaysAgo)) {
                throw new GeneralException(ErrorStatus._WITHDRAWAL_RESTRICTION);
            }
            userRepository.delete(user);
            userRepository.flush();
            user = registerUser(socialUser);
        }

        // 앱 토큰(JWT) 발급
        String appAccessToken = jwtProvider.createAccessToken(user.getId(), user.getRole());
        String appRefreshToken = jwtProvider.createRefreshToken(user.getId());

        saveRefreshToken(user, appRefreshToken);

        return AuthResponse.builder()
                .accessToken(appAccessToken)
                .refreshToken(appRefreshToken)
                .isRegistered(user.getRole() == Role.USER)
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }

    // 소셜 서버에 요청해서 인가 코드를 토큰으로 교환
    private String getSocialAccessToken(String provider, String code) {
        String tokenUri;
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");

        switch (provider.toLowerCase()) {
            case "kakao":
                tokenUri = "https://kauth.kakao.com/oauth/token";
                params.add("client_id", kakaoClientId);
                params.add("client_secret", kakaoClientSecret);
                params.add("redirect_uri", kakaoRedirectUri);
                break;
            case "naver":
                tokenUri = "https://nid.naver.com/oauth2.0/token";
                params.add("client_id", naverClientId);
                params.add("client_secret", naverClientSecret);
                params.add("redirect_uri", naverRedirectUri);
                params.add("state", "test_state");
                break;
            case "google":
                tokenUri = "https://oauth2.googleapis.com/token";
                params.add("client_id", googleClientId);
                params.add("client_secret", googleClientSecret);
                params.add("redirect_uri", googleRedirectUri);
                break;
            default:
                throw new GeneralException(ErrorStatus._INVALID_PROVIDER);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("access_token").asText();
        } catch (Exception e) {
            log.error("소셜 로그인 토큰 발급 실패: {}", e.getMessage());
            throw new GeneralException(ErrorStatus._SOCIAL_LOGIN_FAILED);
        }
    }*/

    @Transactional
    public AuthResponse login(String provider, String token) {
        // provider 검증 -> 400 처리
        if (!isSupportedProvider(provider)) {
            throw new GeneralException(ErrorStatus._INVALID_PROVIDER);
        }

        // 토큰 값 기본 검증 -> 401로 처리(인증 실패)
        if (token == null || token.isBlank()) {
            throw new GeneralException(ErrorStatus._UNAUTHORIZED);
        }

        // 1. 소셜 서버에서 사용자 정보(식별자) 가져오기
        SocialUserInfo socialUser = socialLoadStrategy.getSocialInfo(provider, token);

        // 2. DB 조회 (없으면 회원가입, 있으면 로그인)
        User user = userRepository.findBySocialTypeAndSocialId(socialUser.getSocialType(), socialUser.getSocialId())
                .orElseGet(() -> registerUser(socialUser));

        //탈퇴한지 7일이 지나지 않은 경우
        if (user.getDeletedAt() != null) {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            if (user.getDeletedAt().isAfter(sevenDaysAgo)) {
                throw new GeneralException(ErrorStatus._WITHDRAWAL_RESTRICTION);
            }
        }

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

    private boolean isSupportedProvider(String provider) {
        return provider != null && (
                provider.equalsIgnoreCase("kakao")
                        || provider.equalsIgnoreCase("naver")
                        || provider.equalsIgnoreCase("google")
        );
    }

    // 신규 유저 저장 (GUEST 권한)
    private User registerUser(SocialUserInfo socialUser) {
        User newUser = User.builder()
                .socialType(socialUser.getSocialType())
                .socialId(socialUser.getSocialId())
                .role(Role.GUEST)
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
            throw new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN);
        }

        // 2. DB에서 해당 Refresh Token을 가진 정보 확인
        UserTokens userTokens = userTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new GeneralException(ErrorStatus._TOKEN_NOT_FOUND));

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
