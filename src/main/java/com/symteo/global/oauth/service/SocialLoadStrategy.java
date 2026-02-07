package com.symteo.global.oauth.service;

import com.symteo.domain.user.enums.SocialType;
import com.symteo.global.oauth.impl.GoogleUserInfo;
import com.symteo.global.oauth.impl.KakaoUserInfo;
import com.symteo.global.oauth.impl.NaverUserInfo;
import com.symteo.global.oauth.info.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocialLoadStrategy {
    private final WebClient webClient = WebClient.create();

    @Value("${spring.oauth.kakao.user-info-uri}")
    private String kakaoUserInfoUri;

    @Value("${spring.oauth.naver.user-info-uri}")
    private String naverUserInfoUri;

    @Value("${spring.oauth.google.user-info-uri}")
    private String googleUserInfoUri;

    public SocialUserInfo getSocialInfo(String providerName, String accessToken) {
        SocialType socialType = SocialType.from(providerName);

        return switch (socialType) {
            case KAKAO -> getKakaoInfo(accessToken);
            case GOOGLE -> getGoogleInfo(accessToken);
            case NAVER -> getNaverInfo(accessToken);
        };
    }

    private SocialUserInfo getKakaoInfo(String accessToken) {
        Map<String, Object> attributes = callSocialApi(kakaoUserInfoUri, accessToken);
        return new KakaoUserInfo(attributes);
    }

    private SocialUserInfo getGoogleInfo(String accessToken) {
        Map<String, Object> attributes = callSocialApi(googleUserInfoUri, accessToken);
        return new GoogleUserInfo(attributes);
    }

    private SocialUserInfo getNaverInfo(String accessToken) {
        Map<String, Object> attributes = callSocialApi(naverUserInfoUri, accessToken);
        return new NaverUserInfo(attributes);
    }

    private Map<String, Object> callSocialApi(String url, String accessToken) {
        return webClient.get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
