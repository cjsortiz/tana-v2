package com.tana.tana_auth.functions.account.service;

import com.tana.tana_auth.functions.account.dto.AccountBasicDetailsDto;
import com.tana.tana_auth.functions.account.dto.AccountMasterRequestDto;
import com.tana.tana_auth.functions.account.dto.AccountMasterResponseDto;
import com.tana.tana_auth.functions.account.dto.AccountResponseDto;
import com.tana.tana_common.constant.exception.TanaException;
import org.springframework.web.multipart.MultipartFile;

public interface AccountService {

    AccountResponseDto getUserProfileDetails() throws TanaException;
    void clearUserDetailsCache(String id);
    AccountMasterResponseDto updateOnboardingStatus(AccountMasterRequestDto requestDto) throws TanaException;
    AccountMasterResponseDto updateProfile(AccountMasterRequestDto dto, MultipartFile file) throws TanaException;
}
