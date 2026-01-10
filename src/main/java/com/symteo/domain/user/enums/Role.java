package com.symteo.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    // 1. 가입은 되었으나 닉네임 등 추가 정보를 입력하지 않은 상태
    GUEST("ROLE_GUEST", "손님"),

    // 2. 닉네임 등록까지 마친 정회원
    USER("ROLE_USER", "일반 사용자"),

    // 3. 관리자 (필요 시 사용)
    ADMIN("ROLE_ADMIN", "관리자");

    private final String key;   // 스프링 시큐리티에서 사용할 키 (ROLE_ 접두사 필수)
    private final String title; // 사람이 읽기 위한 설명 (UI 표시용)
}
