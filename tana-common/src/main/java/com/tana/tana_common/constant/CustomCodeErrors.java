package com.tana.tana_common.constant;

public enum CustomCodeErrors {
    TOO_MANY_REQUESTS(429,"","Too many requests . Try again later"),
    BAD_CREDENTIALS(null, "badCredentials","Bad Credentials"),
    ACCOUNT_LOCKED(null, "accountLocked","Account is locked"),
    GENERIC_ERROR(null,"genericError","Somethings wrong with the data. Please try again"),
    RECORD_NOT_EXIST(null,"recordNotExist","Record does not exist"),
    USER_ALR_EXIST(null,"userExist","User already exists"),
    EMAIL_ALR_EXIST(null,"emailExist","Email already exists");


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
