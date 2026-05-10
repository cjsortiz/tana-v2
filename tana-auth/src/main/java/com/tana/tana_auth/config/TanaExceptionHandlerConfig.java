package com.tana.tana_auth.config;

import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class TanaExceptionHandlerConfig {

    @ExceptionHandler(TanaException.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody TanaApiResponse handleCustomException(TanaException ex, HttpServletRequest request) {

        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add(ex.getErrorMessage());
        errorCodes.add(ex.getMessageCode());

        return TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA CUSTOM EXCEPTION")
                .errorMessages(errorMessages)
                .build();
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody TanaApiResponse handleException(Exception ex, HttpServletRequest request) {

        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add(ex.getMessage());
        errorCodes.add("TANA EXCEPTION");

        System.out.println(ex.getMessage());

        return TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA EXCEPTION")
                .errorMessages(errorMessages)
                .build();
    }

    @ExceptionHandler(BadCredentialsException.class)
    public @ResponseBody TanaApiResponse handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add(ex.getMessage());
        errorCodes.add("TANA BAD CREDENTIALS ERROR");

        return TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA BAD CREDENTIALS ERROR")
                .errorMessages(errorMessages)
                .build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody TanaApiResponse handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add(ex.getMessage());
        errorCodes.add("TANA FORBIDDEN ERROR");

        return TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA FORBIDDEN ERROR")
                .errorMessages(errorMessages)
                .build();
    }
}
