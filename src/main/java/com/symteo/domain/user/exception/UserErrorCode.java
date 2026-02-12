package com.symteo.domain.user.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    // User 관련 에러
    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4041", "사용자를 찾을 수 없습니다."),
    _USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER4011", "인증되지 않은 사용자입니다."),

    // Nickname 관련 에러
    _NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "USER4001", "닉네임을 입력해주세요."),
    _NICKNAME_INVALID(HttpStatus.BAD_REQUEST, "USER4002", "닉네임은 한글, 영문, 숫자로만 구성된 3~10자여야 합니다."),
    _NICKNAME_CONFLICT(HttpStatus.CONFLICT, "USER4091", "이미 사용 중인 닉네임입니다."),

    // Settings 관련 에러
    _USER_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4042", "사용자 설정을 찾을 수 없습니다."),

    // Mission 관련 에러
    _USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4043", "사용자의 미션 기록을 찾을 수 없습니다."),
    _MISSION_FORBIDDEN(HttpStatus.FORBIDDEN, "USER4031", "해당 미션에 대한 접근 권한이 없습니다."),
    _DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4044", "임시저장 내용을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }
}

