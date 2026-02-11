package com.symteo.domain.diagnose.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DiagnoseErrorCode implements BaseErrorCode {

    _DIAGNOSE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "DIAGNOSE403", "해당 진단에 권한이 없습니다."),
    _DIAGNOSE_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSE404","해당 분야의 진단이 존재하지 않습니다.");

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
