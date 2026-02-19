package com.symteo.domain.user.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    _USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "사용자를 찾을 수 없습니다."),
    _NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "USER400_1", "닉네임을 입력해주세요."),
    _NICKNAME_INVALID(HttpStatus.BAD_REQUEST, "USER400_2", "닉네임은 한글/영문/숫자 3~10자로 입력해주세요."),
    _NICKNAME_CONFLICT(HttpStatus.CONFLICT, "USER409", "이미 사용 중인 닉네임입니다."),
    _USER_SETTINGS_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_1", "사용자 설정을 찾을 수 없습니다."),
    _USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_2", "해당 미션을 찾을 수 없습니다."),
    _DRAFT_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404_3", "미션 초안을 찾을 수 없습니다.");

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

