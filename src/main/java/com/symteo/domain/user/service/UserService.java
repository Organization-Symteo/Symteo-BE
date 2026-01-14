package com.symteo.domain.user.service;

import com.symteo.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    // 닉네임 규칙: 한글/영문/숫자 포함, 3~10자, 특수문자/공백 금지
    private static final String NICKNAME_REGEX = "^[가-힣a-zA-Z0-9]{3,10}$";
    private static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);

    public boolean checkNicknameDuplication(String nickname) {
        // 1. 빈 값 체크(사용자가 값을 입력을 하지 않은 경우)
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        // 2. 유효성 검사 (정규식)
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw new IllegalArgumentException("닉네임은 특수문자를 제외한 3~10자리여야 합니다.");
        }

        // 3. 중복 검사 (DB에 이미 저장된 닉네임인 경우)
        if (userRepository.existsByNickname(nickname)) {
            return true; // 중복됨 (사용 불가)
        }

        return false; // 중복 안 됨 (사용 가능)
    }

}
