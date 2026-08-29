package com.tana.tana_auth.functions.account.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeSpotsDto {
    private String spotName;
    private Integer isVisited;
    private List<String> spotImages;
}
