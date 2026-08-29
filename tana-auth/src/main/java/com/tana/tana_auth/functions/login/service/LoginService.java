package com.tana.tana_auth.functions.login.service;

import com.tana.tana_auth.functions.login.dto.LoginRequestDto;
import com.tana.tana_auth.functions.login.dto.LoginResponseDto;
import com.tana.tana_auth.functions.login.dto.GoogleLoginRequestDto;
import com.tana.tana_auth.functions.login.dto.AppleLoginRequestDto;
import com.tana.tana_auth.functions.login.dto.RefreshTokenDto;
import org.springframework.stereotype.Service;

@Service
public interface LoginService {


    LoginResponseDto login(LoginRequestDto loginRequestDto);

    LoginResponseDto loginWithGoogle(GoogleLoginRequestDto googleLoginRequestDto);

    LoginResponseDto loginWithApple(AppleLoginRequestDto appleLoginRequestDto);

    /**
     * Delete Session if user has been logged out to the system
     *
     * @param refreshTokenDto {@link RefreshTokenDto} Contains the refresh token
     */
    void deleteSession(RefreshTokenDto refreshTokenDto);

    void saveEmail(String email);

}
