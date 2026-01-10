package com.symteo.domain.user.enums;

public enum SocialType {
    KAKAO, NAVER, GOOGLE;

    // 문자열에서 Enum으로 변환하는 메서드(API 호출용)
    public static SocialType from(String provider) {
        try {
            return SocialType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("지원하지 않는 소셜 타입입니다: " + provider);
        }
    }
}
