package com.tana.tana_auth.functions.account.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeSpotsDto {
    private String spotName;
    private Integer isVisited;
}
