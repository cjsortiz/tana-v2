package com.tana.tana_auth.functions.places.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReflectionResponseDto {
    private String name;
    private LocalDateTime date;
    private String content;
    private boolean isMine;
}