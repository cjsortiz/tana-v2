package com.tana.tana_auth.functions.login.service.impl;

import com.tana.tana_auth.functions.login.dto.LoginRequestDto;
import com.tana.tana_auth.functions.login.dto.LoginResponseDto;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.login.dto.RefreshTokenDto;
import com.tana.tana_auth.functions.login.repository.NotifyEmailRepository;
import com.tana.tana_auth.functions.login.service.LoginService;
import com.tana.tana_auth.functions.login.service.SessionService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.functions.userdetails.UserDetailsImpl;
import com.tana.tana_common.model.NotifyEmail;
import com.tana.tana_common.model.SessionToken;
import com.tana.tana_common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private AccountMasterRepository accountMasterRepository;

    @Autowired
    private NotifyEmailRepository notifyEmailRepository;


    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequestDto) throws TanaException{
        Authentication authentication = getAuthentication(loginRequestDto.getUsername(), loginRequestDto.getPassword());

        if (!ObjectUtils.isEmpty(authentication) && accountMasterRepository.isIpBlocked(loginRequestDto.getUsername())) {
            throw new TanaException(CustomCodeErrors.ACCOUNT_LOCKED);
        }

        UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();

        // Generate the JWT
        String jwt = jwtUtil.generateJwtToken(user.getUsername(), "user");

        // Save the session
        saveSession(user.getUsername(), jwt, jwt, null);


        return generateResponse(jwt, jwt, user);

    }

    /**
     * Delete Session if user has been logged out to the system
     *
     * @param refreshTokenDto {@link RefreshTokenDto} Contains the refresh token
     */
    @Override
    public void deleteSession(RefreshTokenDto refreshTokenDto) {
        Object sessionByRefreshToken = findByRefreshToken(refreshTokenDto.getRefreshToken());

        if (!ObjectUtils.isEmpty(sessionByRefreshToken)) {
            SessionToken session = (SessionToken) sessionByRefreshToken;
            sessionService.delete(session);
        }
    }

    @Override
    public void saveEmail(String email) throws TanaException{
        NotifyEmail existing =  notifyEmailRepository.findByEmail(email);
        if(!ObjectUtils.isEmpty(existing)) {
            throw new TanaException(CustomCodeErrors.EMAIL_ALR_EXIST);
        }

        existing = new NotifyEmail();
        existing.setEmail(email);
        notifyEmailRepository.save(existing);
    }

    /**
     * Retrieve a Session entity by refresh token.
     *
     * @param refreshToken The refresh token associated with the session.
     * @return The Session or TerminalSession entity if found, or null if not found.
     */
    private Object findByRefreshToken(String refreshToken) {
            return sessionService.findByRefreshToken(refreshToken);
    }

    private Authentication getAuthentication(final String username, final String password) throws TanaException {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return authentication;
        } catch (BadCredentialsException e) {
            throw new TanaException(CustomCodeErrors.BAD_CREDENTIALS);
        } catch (LockedException e) {

            if (accountMasterRepository.isIpBlocked(username)) {
                throw new TanaException(CustomCodeErrors.ACCOUNT_LOCKED);
            }
            throw new TanaException(CustomCodeErrors.ACCOUNT_LOCKED);

        }  catch (Exception e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    private LoginResponseDto generateResponse(final String jwt, final String refreshToken, final UserDetailsImpl userDetails) {
        return LoginResponseDto.builder()
                .token(jwt)
                .refreshToken(refreshToken)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .image(userDetails.getImage())
                .displayName(userDetails.getDisplayName())
                .image64(userDetails.getImage64())
                .adminAccess(userDetails.isAdminAccess())
                .accessLevel(userDetails.getAccessLevel().name())
                .isOnboarded(userDetails.isOnboarded())
                .dropdownMasterList(userDetails.getDropdownMasterList())
                .build();
    }

    private void saveSession(final String username, final String token, final String refreshToken, final String companyId) {
        // Delete the previous session and create a new one
        var session = new SessionToken();
        session.setTokenString(token);
        if (!ObjectUtils.isEmpty(refreshToken)) {
            session.setRefreshTokenString(refreshToken);
        }
        session.setStartTime(LocalDateTime.now());
        sessionService.clearOtherSessionAndSave(session, username);
    }

}
