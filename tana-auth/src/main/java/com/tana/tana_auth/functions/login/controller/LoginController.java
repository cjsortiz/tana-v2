package com.tana.tana_auth.functions.login.controller;

import com.tana.tana_auth.functions.login.dto.LoginRequestDto;
import com.tana.tana_auth.functions.login.dto.RefreshTokenDto;
import com.tana.tana_auth.functions.login.service.LoginService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping(value = "/login")
    private TanaApiResponse testGetController(@RequestAttribute("validated") LoginRequestDto requestDto) {
        return TanaApiResponse.builder()
                .isSuccess(true)
                .resultData(loginService.login(requestDto))
                .build();
    }

    /**
     * Delete Session if user has been logged out to the system
     *
     * @param refreshTokenDto {@link RefreshTokenDto} Contains the refresh token
     */
    @PostMapping(value = "/logout")
    @ResponseStatus(HttpStatus.OK)
    public TanaApiResponse deleteSession(@RequestAttribute("validated") RefreshTokenDto refreshTokenDto) {
        loginService.deleteSession(refreshTokenDto);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }


    @PostMapping(value = "/notify/{email}")
    private TanaApiResponse saveEmail(@PathVariable("email") String email) {
        loginService.saveEmail(email);
        return TanaApiResponse.builder()
            .isSuccess(true)
            .build();
    }
}
