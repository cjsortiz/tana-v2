package com.tana.tana_auth.functions.settings.service;

import com.tana.tana_auth.functions.settings.dto.SettingsRequestDto;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.stereotype.Service;

@Service
public interface SettingsService {

    void upsertSetting(SettingsRequestDto requestDto) throws TanaException;

    String getMapBoxApi();
}
