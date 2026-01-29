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
    _INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),

    // For test
    TEMP_EXCEPTION(HttpStatus.BAD_REQUEST, "TEMP4001", "이거는 테스트"),

    // Mission 오류
    _MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION404","오늘의 미션이 없습니다."),
    _MISSION_EXPIRED(HttpStatus.GONE, "MISSION410", "미션 기한이 만료되었습니다."),
    _USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_MISSION404", "진행 중인 미션이 아닙니다."),
    _IMAGE_URL_MISSING(HttpStatus.BAD_REQUEST, "MISSION400", "선택된 이미지가 없습니다."),
    _IMAGE_UPLOAD_FAILED(HttpStatus.BAD_REQUEST, "MISSION400", "이미지 업로드에 실패했습니다."),
    _MISSION_REFRESH_EXCEEDED(HttpStatus.CONFLICT, "MISSION409", "이미 새로고침했습니다."),
    _NO_MORE_MISSIONS(HttpStatus.BAD_REQUEST, "MISSION400", "더 이상 미션이 없습니다."),
    _MISSION_ALREADY_COMPLETED(HttpStatus.CONFLICT, "MISSION409", "이미 완료한 미션입니다."),

    // Member 없음 오류
    _MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "사용자가 없습니다."),

    // User 오류
    // _MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER404", "사용자가 없습니다."),

    _NICKNAME_INVALID(HttpStatus.BAD_REQUEST, "USER400", "닉네임은 특수문자를 제외한 3~10자리여야 합니다."),
    _NICKNAME_EMPTY(HttpStatus.BAD_REQUEST, "USER400", "닉네임을 입력해주세요."),

    // 닉네임 중복 오류
    _NICKNAME_CONFLICT(HttpStatus.CONFLICT, "USER409", "이미 사용 중인 닉네임입니다."),

    // AI 상담사 중복 오류
    COUNSELOR_ALREADY_EXISTS(HttpStatus.CONFLICT, "COUNSELOR409", "이미 상담사 설정이 존재합니다."),
    COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "COUNSELOR404", "상담사 설정을 찾을 수 없습니다."),
    _WITHDRAWAL_RESTRICTION(HttpStatus.FORBIDDEN, "USER403", "탈퇴 후 7일간 재가입할 수 없습니다."),
    _TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "USER404", "토큰을 찾을 수 없습니다."),

    // 진단 오류
    _DIAGNOSE_NOT_FOUND(HttpStatus.NOT_FOUND, "DIAGNOSE404", "검사 결과가 없습니다."),
    _REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "REPORTS404", "리포트가 존재하지 않습니다.");

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