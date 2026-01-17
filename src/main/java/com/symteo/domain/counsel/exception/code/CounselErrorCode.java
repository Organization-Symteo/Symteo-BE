package com.symteo.domain.counsel.exception.code;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CounselErrorCode implements BaseErrorCode {

    _CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM404","채팅방이 존재하지 않습니다."),
    _CHATMESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATMESSAGE404","채팅이 존재하지 않습니다."),
    _AI_SERVER_ERROR(HttpStatus.REQUEST_TIMEOUT, "COUNSEL408", "AI 서버와의 연결이 원활하지 않습니다.");

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
