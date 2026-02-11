package com.symteo.domain.report.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReportsErrorCode implements BaseErrorCode {
    _REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORT4041", "리포트가 존재하지 않습니다."),
    _REPORT_FORBIDDEN(HttpStatus.FORBIDDEN, "REPORT4031", "해당 리포트에 대한 접근 권한이 없습니다."),
    _AI_ANALYSIS_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "REPORT5001", "AI 분석 도중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder().message(message).code(code).isSuccess(false).build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder().message(message).code(code).isSuccess(false).httpStatus(httpStatus).build();
    }
}