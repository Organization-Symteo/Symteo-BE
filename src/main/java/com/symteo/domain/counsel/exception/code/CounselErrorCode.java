package com.symteo.domain.counsel.exception.code;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CounselErrorCode implements BaseErrorCode {

    _CHATROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHATROOM403", "해당 채팅방에 권한이 없습니다."),
    _CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATROOM404","채팅방이 존재하지 않습니다."),
    _CHATMESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATMESSAGE404","채팅이 존재하지 않습니다."),
    _AI_SERVER_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "COUNSEL503", "AI 서버와의 연결이 원활하지 않습니다."),
    _COUNSELOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "COUNSELOR409", "이미 상담사 설정이 존재합니다."),
    _COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNSELOR404", "상담사 설정을 찾을 수 없습니다."),
    _SETTING_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SETTING500", "상담사 설정 저장 요청이 실패했습니다."),
    _SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTING404", "해당 사용자의 상담사 초기 설정이 존재하지 않습니다."),

    _REDIS_SETTING_NOT_FOUND(HttpStatus.NOT_FOUND, "SETTING404", "Redis 캐시에서 상담자 설정이 존재하지 않습니다."),
    _REDIS_SETTING_NOT_SAVED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_SETTING500", "Redis 캐시에서 상담사 설정이 저장되지 않았습니다"),
    _REDIS_CHATMESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHATMESSAGE404","Redis 캐시에서 채팅이 존재하지 않습니다."),
    _REDIS_CHATMESSAGE_NOT_SAVED(HttpStatus.INTERNAL_SERVER_ERROR, "REDIS_CHATMESSAGE500", "Redis 캐시에서 채팅이 저장되지 않았습니다");

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
