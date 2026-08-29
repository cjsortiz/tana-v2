package com.tana.tana_common.constant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailPhotoAttachment {

    private String fileName;

    private String contentType;

    private byte[] bytes;
}
