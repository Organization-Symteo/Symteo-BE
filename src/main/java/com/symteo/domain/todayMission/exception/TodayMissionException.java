package com.symteo.domain.todayMission.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;

public class TodayMissionException extends GeneralException {
    public TodayMissionException(BaseErrorCode code) {
        super(code);
    }
}
