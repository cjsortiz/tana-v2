package com.tana.tana_common.constant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TanaApiResponse {

    private Boolean isSuccess;

    private Object resultData;

    private Object errorCodes;

    private List<String> errorMessages;

    private String exceptionType;

}
