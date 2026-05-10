package com.tana.tana_auth.functions.registration.service;

import com.tana.tana_auth.functions.registration.dto.RegistrationDtoRequest;
import org.springframework.stereotype.Service;

@Service
public interface RegistrationService {

    void register(RegistrationDtoRequest request) throws Exception ;
}
