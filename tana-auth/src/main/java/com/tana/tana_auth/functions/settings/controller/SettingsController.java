package com.tana.tana_auth.functions.settings.controller;

import com.tana.tana_auth.functions.settings.dto.SettingsRequestDto;
import com.tana.tana_auth.functions.settings.service.SettingsService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "auth/settings")
public class SettingsController {

    @Autowired
    private SettingsService service;

    @PostMapping
    private TanaApiResponse upsertSetting(
        @RequestAttribute("validated") SettingsRequestDto request
    ) throws TanaException {
        service.upsertSetting(request);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }


    @GetMapping
    private TanaApiResponse getMapBoxApi() throws TanaException {
        return TanaApiResponse.builder()
            .resultData(service.getMapBoxApi())
            .isSuccess(true)
            .build();
    }
}
