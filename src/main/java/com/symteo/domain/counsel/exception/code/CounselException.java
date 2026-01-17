package com.symteo.domain.counsel.exception.code;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;

public class CounselException extends GeneralException {
    public CounselException(BaseErrorCode code) {
        super(code);
    }
}
