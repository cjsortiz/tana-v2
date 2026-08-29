package com.tana.tana_auth.config;

import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Slf4j
public class TanaExceptionHandlerConfig {

    @ExceptionHandler(TanaException.class)
    public @ResponseBody ResponseEntity<TanaApiResponse> handleCustomException(TanaException ex, HttpServletRequest request) {

        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add(ex.getErrorMessage());
        errorCodes.add(ex.getMessageCode());

        log.warn(
                "TanaException handled: method={}, uri={}, code={}, message={}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessageCode(),
                ex.getErrorMessage()
        );

        TanaApiResponse response = TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA CUSTOM EXCEPTION")
                .errorMessages(errorMessages)
                .build();

        return ResponseEntity
                .status(resolveStatus(ex))
                .body(response);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public @ResponseBody TanaApiResponse handleException(Exception ex, HttpServletRequest request) {

        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add("Something went wrong. Please try again.");
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
    public @ResponseBody ResponseEntity<TanaApiResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add("Email/username or password is incorrect.");
        errorCodes.add("badCredentials");

        TanaApiResponse response = TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA BAD CREDENTIALS ERROR")
                .errorMessages(errorMessages)
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public @ResponseBody ResponseEntity<TanaApiResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        List<String> errorMessages = new ArrayList<>();
        List<String> errorCodes = new ArrayList<>();

        errorMessages.add("You do not have permission to perform this action.");
        errorCodes.add("forbidden");

        TanaApiResponse response = TanaApiResponse.builder()
                .isSuccess(false)
                .errorCodes(errorCodes)
                .exceptionType("TANA FORBIDDEN ERROR")
                .errorMessages(errorMessages)
                .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    private HttpStatus resolveStatus(TanaException ex) {
        if (ex.getCode() == null) {
            return HttpStatus.BAD_REQUEST;
        }

        HttpStatus status = HttpStatus.resolve(ex.getCode());
        return status != null ? status : HttpStatus.BAD_REQUEST;
    }
}
