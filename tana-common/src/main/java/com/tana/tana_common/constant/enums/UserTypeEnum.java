package com.tana.tana_common.constant.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserTypeEnum {

    VISITOR("visitor", 1),
    LOCAL("local", 2);

    private final String code;
    private final int value;

    public static UserTypeEnum fromCode(String code) {
        for (UserTypeEnum type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid user type: " + code);
    }
}