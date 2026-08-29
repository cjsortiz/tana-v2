package com.tana.tana_auth.functions.routes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RoutePartnerRequestDto {
    private String partnerName;
    private String description;
    private String logoImage;
    private String helperText;
    private Boolean active;
}
