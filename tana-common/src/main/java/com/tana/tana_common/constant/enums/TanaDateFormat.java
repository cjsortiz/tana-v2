package com.tana.tana_common.constant.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TanaDateFormat {

    MMDDYYYY("MMddYYYY"),

    YYYYMMDD("yyyyMMdd"),

    YYYYMMDDHHMMSS("yyyyMMddHHmmss"),

    YYYYMMDD_DASH("yyyy-MM-dd"),

    YYYYMMDD_DASH_T_HHMM_COLON("yyyy-MM-dd'T'HH:mm:ss"),

    YYYYMMDD_DASH_HHMM_COLON("yyyy-MM-dd HH:mm:ss"),

    MMDDYYYY_SLASH("MM/dd/yyyy"),

    YYYYMMDD_SLASH("yyyy/MM/dd"),

    HHMM("HHmm"),

    HHMM_COLON("HH:mm"),

    MMDDYYYY_SLASH_HHMM_COLON("MM/dd/yyyy HH:mm"),

    MMMDDYYSS("EEE MMM dd yyyy HH:mm:ss 'GMT'Z"),

    YY("yy"),

    HHMMSS_COLON("HH:mm:ss");

    private final String value;
}