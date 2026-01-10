package com.symteo.global.oauth.impl;

import com.symteo.domain.user.enums.SocialType;
import com.symteo.global.oauth.info.SocialUserInfo;

import java.util.Map;

public class NaverUserInfo implements SocialUserInfo {
    private final Map<String, Object> attributes; // 전체 응답
    private final Map<String, Object> response;   // "response" 내부

    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        this.response = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public String getSocialId() {
        if (response == null) {
            return null;
        }
        return (String) response.get("id");
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.NAVER;
    }
}
