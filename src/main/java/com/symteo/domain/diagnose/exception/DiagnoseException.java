package com.symteo.domain.diagnose.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;

public class DiagnoseException extends GeneralException {
    public DiagnoseException(BaseErrorCode code) {
        super(code);
    }
}
