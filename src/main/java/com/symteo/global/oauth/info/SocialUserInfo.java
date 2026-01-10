package com.symteo.global.oauth.info;

import com.symteo.domain.user.enums.SocialType;

public interface SocialUserInfo {
    String getSocialId();
    SocialType getSocialType();
}
