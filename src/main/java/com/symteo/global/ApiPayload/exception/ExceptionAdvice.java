package com.symteo.global.ApiPayload.exception;


import com.symteo.global.ApiPayload.ApiResponse;
import com.symteo.global.ApiPayload.code.ErrorReasonDTO;
import com.symteo.global.ApiPayload.status.ErrorStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    // 부모 클래스(ResponseEntityExceptionHandler)가 처리하는 모든 Spring 예외의 최종 경로
    // Spring 기본 예외(JSON 파싱 실패, HTTP 메서드 불일치, 파라미터 누락 등)도 ApiResponse 공통 포맷으로 변환
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

        // 이미 ApiResponse로 만들어진 body는 그대로 통과
        if (body instanceof ApiResponse) {
            return super.handleExceptionInternal(ex, body, headers, statusCode, request);
        }

        // 그 외 Spring 기본 예외들 → 공통 포맷으로 변환
        HttpStatus status = HttpStatus.resolve(statusCode.value());
        String errorCode = (status != null && status.is4xxClientError())
                ? ErrorStatus._BAD_REQUEST.getCode()
                : ErrorStatus._INTERNAL_SERVER_ERROR.getCode();

        ApiResponse<Object> apiResponse = ApiResponse.onFailure(
                errorCode,
                resolveKoreanMessage(ex),
                null
        );

        return super.handleExceptionInternal(ex, apiResponse, headers, statusCode, request);
    }

    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(constraintViolation -> constraintViolation.getMessage())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        return handleExceptionInternalConstraint(e, ErrorStatus.valueOf(errorMessage), HttpHeaders.EMPTY,request);
    }


    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors().stream()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage, (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        return handleExceptionInternalArgs(e,HttpHeaders.EMPTY,ErrorStatus.valueOf("_BAD_REQUEST"),request,errors);
    }

    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
//        e.printStackTrace();

        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(),request, e.getMessage());
    }

    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> onThrowException(GeneralException generalException, HttpServletRequest request) {

        ErrorReasonDTO errorReasonHttpStatus = generalException.getErrorReasonHttpStatus();
        return handleExceptionInternal(generalException,errorReasonHttpStatus,null,request);
    }

    private ResponseEntity<Object> handleExceptionInternal(Exception e, ErrorReasonDTO reason,
                                                           HttpHeaders headers, HttpServletRequest request) {

        ApiResponse<Object> body = ApiResponse.onFailure(reason.getCode(),reason.getMessage(),null);
//        e.printStackTrace();

        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                reason.getHttpStatus(),
                webRequest
        );
    }

    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, ErrorStatus errorCommonStatus,
                                                                HttpHeaders headers, HttpStatus status, WebRequest request, String errorPoint) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorPoint);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                status,
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalArgs(Exception e, HttpHeaders headers, ErrorStatus errorCommonStatus,
                                                               WebRequest request, Map<String, String> errorArgs) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(),errorCommonStatus.getMessage(),errorArgs);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    private ResponseEntity<Object> handleExceptionInternalConstraint(Exception e, ErrorStatus errorCommonStatus,
                                                                     HttpHeaders headers, WebRequest request) {
        ApiResponse<Object> body = ApiResponse.onFailure(errorCommonStatus.getCode(), errorCommonStatus.getMessage(), null);
        return super.handleExceptionInternal(
                e,
                body,
                headers,
                errorCommonStatus.getHttpStatus(),
                request
        );
    }

    /**
     * Spring 기본 예외 → 한국어 메시지 변환
     */
    private String resolveKoreanMessage(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요.";
        }
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            return "지원하지 않는 HTTP 메서드입니다.";
        }
        if (ex instanceof HttpMediaTypeNotSupportedException) {
            return "지원하지 않는 Content-Type입니다. application/json으로 요청해주세요.";
        }
        if (ex instanceof MissingServletRequestParameterException e) {
            return "필수 요청 파라미터 '" + e.getParameterName() + "'이(가) 누락되었습니다.";
        }
        if (ex instanceof MissingPathVariableException e) {
            return "필수 경로 변수 '" + e.getVariableName() + "'이(가) 누락되었습니다.";
        }
        if (ex instanceof MethodArgumentTypeMismatchException e) {
            return "파라미터 '" + e.getName() + "'의 타입이 올바르지 않습니다.";
        }
        if (ex instanceof NoResourceFoundException) {
            return "요청한 리소스를 찾을 수 없습니다.";
        }
        return "잘못된 요청입니다.";
    }
}