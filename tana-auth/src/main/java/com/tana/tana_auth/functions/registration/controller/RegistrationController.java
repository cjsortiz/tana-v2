package com.tana.tana_auth.functions.registration.controller;

import com.tana.tana_auth.functions.registration.dto.RegistrationDtoRequest;
import com.tana.tana_auth.functions.registration.service.RegistrationService;
import com.tana.tana_common.constant.dto.TanaApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "auth/registration")
public class RegistrationController {

    final private RegistrationService service;

    public RegistrationController(RegistrationService serviceObj){
        this.service = serviceObj;
    }

    @PostMapping()
    private TanaApiResponse register(@RequestAttribute("validated") RegistrationDtoRequest request) throws Exception{
        service.register(request);
        return TanaApiResponse.builder()
                .isSuccess(true)
                .build();
    }
}
