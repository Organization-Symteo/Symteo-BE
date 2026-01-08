package com.symteo.global.ApiPayload.status;

import com.symteo.global.ApiPayload.code.BaseCode;
import com.symteo.global.ApiPayload.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
//성공코드이기 때문에 BaseCode를 구현한다. (실패코드면 BaseErrorCode)
public enum SuccessStatus implements BaseCode {

    // enum중 하나 _OK 이라는 이넘값
    _OK(HttpStatus.OK, "2000", "Ok"),;

    // 이넘 값도 필드를 가질수 있으며 롬복에 의해 생성자로 주입된다.
    //HttpStatus는 Spring에 내장되어있는 클래스로 우리가 그냥 에러 뱉을때 사용하는 클래스이다.
    private HttpStatus httpStatus;

    //code는 우리가 일관성 있게 만들어 줘야 한다.
    private String code;

    //message를 이용해 상태를 알려줄수 있다.
    private String message;


    @Override
    public ReasonDTO getReason() {
        return ReasonDTO.builder()
                .code(code)
                .message(message)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }

    @Override
    public ReasonDTO getReasonHttpStatus() {
        return ReasonDTO.builder()
                .code(code)
                .message(message)
                .isSuccess(true)
                .httpStatus(httpStatus)
                .build();
    }
}