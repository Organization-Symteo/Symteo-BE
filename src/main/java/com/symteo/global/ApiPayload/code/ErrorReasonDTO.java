package com.symteo.global.ApiPayload.code;

import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@Builder
@Getter
@AllArgsConstructor
public class ErrorReasonDTO {
    private final HttpStatus httpStatus;

    private final Boolean isSuccess;
    private final String code;
    private final String message;

}