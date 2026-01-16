package com.symteo.global.ApiPayload.status;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트"),

    // Mission 오류
    _MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION404","오늘의 미션이 없습니다."),
    _MISSION_EXPIRED(HttpStatus.GONE, "MISSION410", "미션 기한이 만료되었습니다."),
    _USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_MISSION404", "진행 중인 미션이 아닙니다."),
    _IMAGE_URL_MISSING(HttpStatus.BAD_REQUEST, "MISSION400", "선택된 이미지가 없습니다."),

    // Member 없음 오류
    _MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "사용자가 없습니다.");

    // Member 없음 오류
//    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다");

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