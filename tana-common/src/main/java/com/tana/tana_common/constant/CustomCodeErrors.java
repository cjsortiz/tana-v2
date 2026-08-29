package com.tana.tana_common.constant;

public enum CustomCodeErrors {
    TOO_MANY_REQUESTS(429,"tooManyRequests","Too many requests. Try again later."),
    BAD_CREDENTIALS(401, "badCredentials","Email/username or password is incorrect."),
    GOOGLE_SIGN_IN_FAILED(401, "googleSignInFailed","Google sign-in could not be verified. Please try again."),
    APPLE_SIGN_IN_FAILED(401, "appleSignInFailed","Apple sign-in could not be verified. Please try again."),
    SESSION_EXPIRED(401, "sessionExpired","Session expired. Please log in again."),
    ACCOUNT_LOCKED(423, "accountLocked","This account is locked."),
    GENERIC_ERROR(400,"genericError","Something is wrong with the submitted data. Please review it and try again."),
    RECORD_NOT_EXIST(404,"recordNotExist","Record not found."),
    USER_ALR_EXIST(409,"userExist","Username already exists."),
    EMAIL_ALR_EXIST(409,"emailExist","Email already exists.");


    final private Integer code;
    final private String stringCode;
    final private String message;

    CustomCodeErrors(Integer code, String stringCode,String message){
        this.stringCode  = stringCode;
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getStringCode() {
        return stringCode;
    }

}
