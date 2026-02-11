package com.symteo.domain.todayMission.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TodayMissionErrorCode implements BaseErrorCode {
    _MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION4041", "미션을 찾을 수 없습니다."),
    _USER_MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MISSION4042", "사용자의 미션 참여 기록을 찾을 수 없습니다."),
    _MISSION_FORBIDDEN(HttpStatus.FORBIDDEN, "MISSION4031", "해당 미션에 대한 접근 권한이 없습니다."),
    _MISSION_EXPIRED(HttpStatus.BAD_REQUEST, "MISSION4001", "미션 수행 시간이 만료되었습니다."),
    _MISSION_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "MISSION4002", "이미 완료된 미션은 수정하거나 새로고침할 수 없습니다."),
    _MISSION_REFRESH_EXCEEDED(HttpStatus.BAD_REQUEST, "MISSION4003", "미션 새로고침은 하루에 한 번만 가능합니다."),
    _NO_MORE_MISSIONS(HttpStatus.NOT_FOUND, "MISSION4043", "새로 할당할 수 있는 미션이 더 이상 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override public ErrorReasonDTO getReason() { return ErrorReasonDTO.builder().message(message).code(code).isSuccess(false).build(); }
    @Override public ErrorReasonDTO getReasonHttpStatus() { return ErrorReasonDTO.builder().message(message).code(code).isSuccess(false).httpStatus(httpStatus).build(); }
}