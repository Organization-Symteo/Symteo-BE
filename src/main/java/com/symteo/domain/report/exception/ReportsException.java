package com.symteo.domain.report.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;

public class ReportsException extends GeneralException {
    public ReportsException(BaseErrorCode code) {
        super(code);
    }
}
