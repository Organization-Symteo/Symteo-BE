package com.symteo.global.oauth.impl;

import com.symteo.domain.user.enums.SocialType;
import com.symteo.global.oauth.info.SocialUserInfo;

import java.util.Map;

public class GoogleUserInfo implements SocialUserInfo {
    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getSocialId() {
        return (String) attributes.get("sub");
    }

    @Override
    public SocialType getSocialType() {
        return SocialType.GOOGLE;
    }
}
