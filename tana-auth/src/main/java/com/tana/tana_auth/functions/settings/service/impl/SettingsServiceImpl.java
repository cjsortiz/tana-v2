package com.tana.tana_auth.functions.settings.service.impl;

import com.tana.tana_auth.functions.settings.dto.SettingsRequestDto;
import com.tana.tana_auth.functions.settings.repository.SettingsRepository;
import com.tana.tana_auth.functions.settings.service.SettingsService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.SettingsMaster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SettingsServiceImpl implements SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    @Override
    public void upsertSetting(SettingsRequestDto requestDto) throws TanaException {
        SettingsMaster settingsMaster = settingsRepository.findByKey(requestDto.getKey())
            .orElseGet(SettingsMaster::new);
        settingsMaster.setSettingsKey(requestDto.getKey());
        settingsMaster.setSettingsValue(requestDto.getVal());
        settingsRepository.save(settingsMaster);
    }

    @Override
    public String getMapBoxApi() {
        SettingsMaster settingsMaster = settingsRepository.findByKey("map-api")
            .orElseThrow(()->  new TanaException(CustomCodeErrors.RECORD_NOT_EXIST));
        return settingsMaster.getSettingsValue();
    }

}
