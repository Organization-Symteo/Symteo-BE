package com.symteo.domain.home.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum HomeErrorCode implements BaseErrorCode {
    _TODAY_LINE_NOT_FOUND(HttpStatus.NOT_FOUND, "HOME4041", "오늘의 한 줄이 존재하지 않습니다."),
    _INVALID_WEATHER_VALUE(HttpStatus.BAD_REQUEST, "HOME4001", "날씨 값은 1에서 4 사이여야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message).code(code).isSuccess(false).build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message).code(code).isSuccess(false).httpStatus(httpStatus).build();
    }
}