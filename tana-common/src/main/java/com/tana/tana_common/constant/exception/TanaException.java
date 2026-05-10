package com.tana.tana_common.constant.exception;

import com.tana.tana_common.constant.CustomCodeErrors;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TanaException extends RuntimeException {

    private String messageCode;

    private String errorMessage;

    public TanaException(CustomCodeErrors errors) {
        super(errors.getMessage());

        this.messageCode = errors.getStringCode();

        this.errorMessage = errors.getMessage();
    }
}

