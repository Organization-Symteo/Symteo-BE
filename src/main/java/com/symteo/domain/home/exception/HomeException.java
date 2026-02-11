package com.symteo.domain.home.exception;

import com.symteo.global.ApiPayload.code.BaseErrorCode;
import com.symteo.global.ApiPayload.exception.GeneralException;

public class HomeException extends GeneralException {
    public HomeException(BaseErrorCode code) {
        super(code);
    }
}
