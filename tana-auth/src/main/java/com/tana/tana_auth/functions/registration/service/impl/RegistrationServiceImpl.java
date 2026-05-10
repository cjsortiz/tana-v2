package com.tana.tana_auth.functions.registration.service.impl;

import com.tana.tana_auth.functions.registration.dto.RegistrationDtoRequest;
import com.tana.tana_auth.functions.registration.repository.RegistrationRepository;
import com.tana.tana_auth.functions.registration.repository.custom.RegistrationCustomMapper;
import com.tana.tana_auth.functions.registration.service.RegistrationService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.EncryptionProperties;
import com.tana.tana_common.constant.enums.AccessLevel;
import com.tana.tana_common.constant.enums.UserTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.util.password.PasswordUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationServiceImpl.class);

    private final RegistrationCustomMapper customRepository;

    private final RegistrationRepository registrationRepository;

    private final static List<String> ADMIN_EMAILS =
        List.of("celmarortiz24@gmail.com", "tiongson.ramchad@gmail.com");

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Autowired
    private PasswordUtil passwordUtil;


    public RegistrationServiceImpl(RegistrationRepository repository, RegistrationCustomMapper custom) {
        this.registrationRepository = repository;
        this.customRepository = custom;

    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void register(RegistrationDtoRequest request) throws Exception {
        AccountMaster existingAcc = registrationRepository.fetchAccountByUserName(request.getUsername());

        final String name = customRepository.getAccountName(request.getUsername());

        log.info(name);

        if (!ObjectUtils.isEmpty(existingAcc)) {
            throw new TanaException(CustomCodeErrors.USER_ALR_EXIST);

        }

        AccountMaster newAccount = new AccountMaster();
        newAccount.setUserName(request.getUsername());
        newAccount.setPreferLang(request.getPreferLang());
        newAccount.setPreferMood(request.getPreferMood());
        if (!ObjectUtils.isEmpty(request.getUserType())) {
            newAccount.setUserType(UserTypeEnum.fromCode(request.getUserType()).getValue());
        }
        newAccount.setPassword(passwordUtil.encrypt(request.getPassword(), encryptionProperties.getKeyConfig(),
            encryptionProperties.getSalt()));
        newAccount.setEmail(request.getEmail());
        newAccount.setActive(true);
        newAccount.setFirstName(request.getFirstName());
        newAccount.setLastName(request.getLastName());
        newAccount.setMiddleName(request.getMiddleName());
        newAccount.setAccessLevel(AccessLevel.USER);

        if (ADMIN_EMAILS.contains(request.getEmail())) {
            newAccount.setAccessLevel(AccessLevel.ADMIN);
        }

        registrationRepository.save(newAccount);
    }
}
