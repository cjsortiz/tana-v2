package com.tana.tana_auth.functions.login.service.impl;

import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.login.repository.SessionRepository;
import com.tana.tana_auth.functions.login.service.SessionService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.SessionToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
public class SessionServiceImpl implements SessionService {

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private SessionRepository repository;

    @Override
    public SessionToken clearOtherSessionAndSave(SessionToken sessionToken, String username){
        AccountMaster user = accountMasterRepository.findByUserName(username);

        if(ObjectUtils.isEmpty(user)){
            throw new TanaException(CustomCodeErrors.RECORD_NOT_EXIST);
        }
        sessionToken.setAccountMaster(user);

        repository.deleteByUserName(user.getId());

        return repository.save(sessionToken);
    }

    /**
     * Retrieve a Session entity by refresh token.
     *
     * @param refreshToken The refresh token associated with the session.
     * @return The Session entity if found, or null if not found.
     */
    public SessionToken findByRefreshToken(final String refreshToken) {
        return repository.findByRefreshToken(refreshToken);
    }

    /**
     * Deletes a Session entity from the repository.
     *
     * @param session The Session entity to delete.
     */
    public void delete(final SessionToken session) {
        repository.delete(session);
    }
}
