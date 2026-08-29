package com.tana.tana_auth.functions.login.service.impl;

import com.tana.tana_auth.functions.login.dto.GoogleLoginRequestDto;
import com.tana.tana_auth.functions.login.dto.AppleLoginRequestDto;
import com.tana.tana_auth.functions.login.dto.GoogleTokenInfoDto;
import com.tana.tana_auth.functions.login.dto.LoginRequestDto;
import com.tana.tana_auth.functions.login.dto.LoginResponseDto;
import com.tana.tana_auth.functions.account.repository.AccountMasterRepository;
import com.tana.tana_auth.functions.login.dto.RefreshTokenDto;
import com.tana.tana_auth.functions.login.repository.NotifyEmailRepository;
import com.tana.tana_auth.functions.login.service.LoginService;
import com.tana.tana_auth.functions.login.service.AppleTokenService;
import com.tana.tana_auth.functions.login.service.SessionService;
import com.tana.tana_common.constant.CustomCodeErrors;
import com.tana.tana_common.constant.EncryptionProperties;
import com.tana.tana_common.constant.enums.AccessLevel;
import com.tana.tana_common.constant.enums.UserTypeEnum;
import com.tana.tana_common.constant.exception.TanaException;
import com.tana.tana_common.functions.userdetails.UserDetailsImpl;
import com.tana.tana_common.functions.userdetails.UserDetailsServiceImpl;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.model.NotifyEmail;
import com.tana.tana_common.model.SessionToken;
import com.tana.tana_common.util.JwtUtil;
import com.tana.tana_common.util.password.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class LoginServiceImpl implements LoginService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";

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

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private EncryptionProperties encryptionProperties;

    @Autowired
    private AppleTokenService appleTokenService;

    @Value("${google.client-ids:}")
    private String googleClientIds;

    @Value("${apple.client-id:com.app.tana}")
    private String appleClientId;

    private volatile JwtDecoder appleJwtDecoder;

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

    @Override
    public LoginResponseDto loginWithGoogle(GoogleLoginRequestDto googleLoginRequestDto) {
        GoogleTokenInfoDto tokenInfo = verifyGoogleToken(googleLoginRequestDto.getIdToken());

        if (ObjectUtils.isEmpty(tokenInfo.getEmail()) ||
            !"true".equalsIgnoreCase(tokenInfo.getEmailVerified())) {
            throw new TanaException(CustomCodeErrors.GOOGLE_SIGN_IN_FAILED);
        }

        AccountMaster account = accountMasterRepository.findByEmail(tokenInfo.getEmail());

        if (ObjectUtils.isEmpty(account)) {
            account = createGoogleAccount(tokenInfo, googleLoginRequestDto.getUserType());
        }

        UserDetailsImpl user = (UserDetailsImpl) userDetailsService.loadUserByUsername(account.getUserName());

        String jwt = jwtUtil.generateJwtToken(user.getUsername(), "user");
        saveSession(user.getUsername(), jwt, jwt, null);

        return generateResponse(jwt, jwt, user);
    }

    @Override
    public LoginResponseDto loginWithApple(AppleLoginRequestDto requestDto) {
        Jwt appleIdentity = verifyAppleToken(requestDto);
        String subject = appleIdentity.getSubject();
        String email = appleIdentity.getClaimAsString("email");

        AccountMaster account = accountMasterRepository.findByAppleSubject(subject);
        if (ObjectUtils.isEmpty(account) && !ObjectUtils.isEmpty(email)) {
            account = accountMasterRepository.findByEmail(email);
        }

        if (ObjectUtils.isEmpty(account)) {
            account = createAppleAccount(appleIdentity, requestDto);
        } else if (ObjectUtils.isEmpty(account.getAppleSubject())) {
            account.setAppleSubject(subject);
        }

        String encryptedRefreshToken =
                appleTokenService.exchangeAuthorizationCode(requestDto.getAuthorizationCode());
        if (!ObjectUtils.isEmpty(encryptedRefreshToken)) {
            account.setAppleRefreshToken(encryptedRefreshToken);
        }
        account = accountMasterRepository.save(account);

        UserDetailsImpl user =
                (UserDetailsImpl) userDetailsService.loadUserByUsername(account.getUserName());
        String jwt = jwtUtil.generateJwtToken(user.getUsername(), "user");
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

    private GoogleTokenInfoDto verifyGoogleToken(String idToken) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(GOOGLE_TOKEN_INFO_URL)
                    .queryParam("id_token", idToken)
                    .toUriString();

            GoogleTokenInfoDto tokenInfo = new RestTemplate().getForObject(url, GoogleTokenInfoDto.class);

            if (ObjectUtils.isEmpty(tokenInfo) || !isAllowedGoogleAudience(tokenInfo.getAud())) {
                throw new TanaException(CustomCodeErrors.GOOGLE_SIGN_IN_FAILED);
            }

            return tokenInfo;
        } catch (RestClientException e) {
            throw new TanaException(CustomCodeErrors.GOOGLE_SIGN_IN_FAILED);
        }
    }

    private boolean isAllowedGoogleAudience(String audience) {
        if (ObjectUtils.isEmpty(audience) || ObjectUtils.isEmpty(googleClientIds)) {
            return false;
        }

        List<String> allowedClientIds = Arrays.stream(googleClientIds.split(","))
                .map(String::trim)
                .filter(clientId -> !clientId.isEmpty())
                .toList();

        return allowedClientIds.contains(audience);
    }

    private Jwt verifyAppleToken(AppleLoginRequestDto requestDto) {
        try {
            Jwt identity = getAppleJwtDecoder().decode(requestDto.getIdentityToken());
            if (!identity.getAudience().contains(appleClientId) ||
                    ObjectUtils.isEmpty(identity.getSubject()) ||
                    !isVerifiedAppleEmail(identity) ||
                    !secureEquals(identity.getClaimAsString("nonce"), requestDto.getNonce()) ||
                    (!ObjectUtils.isEmpty(requestDto.getAppleUserId()) &&
                            !identity.getSubject().equals(requestDto.getAppleUserId()))) {
                throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
            }
            return identity;
        } catch (JwtException | IllegalArgumentException exception) {
            throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
        }
    }

    private boolean isVerifiedAppleEmail(Jwt identity) {
        Object emailVerified = identity.getClaim("email_verified");
        return !ObjectUtils.isEmpty(identity.getClaimAsString("email")) &&
                (Boolean.TRUE.equals(emailVerified) ||
                        "true".equalsIgnoreCase(String.valueOf(emailVerified)));
    }

    private boolean secureEquals(String actual, String expected) {
        if (ObjectUtils.isEmpty(actual) || ObjectUtils.isEmpty(expected)) {
            return false;
        }
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private JwtDecoder getAppleJwtDecoder() {
        if (appleJwtDecoder == null) {
            synchronized (this) {
                if (appleJwtDecoder == null) {
                    appleJwtDecoder = JwtDecoders.fromIssuerLocation(APPLE_ISSUER);
                }
            }
        }
        return appleJwtDecoder;
    }

    private AccountMaster createAppleAccount(
            Jwt identity,
            AppleLoginRequestDto requestDto
    ) {
        String email = identity.getClaimAsString("email");
        if (ObjectUtils.isEmpty(email)) {
            throw new TanaException(CustomCodeErrors.APPLE_SIGN_IN_FAILED);
        }

        try {
            AccountMaster account = new AccountMaster();
            account.setUserName(createAppleUsername(email, identity.getSubject()));
            account.setEmail(email);
            account.setAppleSubject(identity.getSubject());
            account.setFirstName(resolveAppleName(requestDto.getFullName(), email));
            account.setPassword(passwordUtil.encrypt(
                    UUID.randomUUID().toString(),
                    encryptionProperties.getKeyConfig(),
                    encryptionProperties.getSalt()));
            account.setPreferLang("en");
            account.setPreferMood("1");
            account.setUserType(resolveUserType(requestDto.getUserType()));
            account.setActive(true);
            account.setAccessLevel(AccessLevel.USER);
            return account;
        } catch (TanaException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    private String createAppleUsername(String email, String subject) {
        String localPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : "apple";
        String base = localPart.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._-]", "");
        if (base.isBlank()) {
            base = "apple";
        }

        String username = base;
        if (!ObjectUtils.isEmpty(accountMasterRepository.findByUserName(username))) {
            String suffix = subject.replaceAll("[^a-zA-Z0-9]", "");
            int maxBaseLength = Math.min(base.length(), 52);
            username = base.substring(0, maxBaseLength) + "_" +
                    suffix.substring(0, Math.min(suffix.length(), 10));
        }
        return username.length() > 64 ? username.substring(0, 64) : username;
    }

    private String resolveAppleName(String fullName, String email) {
        String resolved = ObjectUtils.isEmpty(fullName) ? email : fullName.trim();
        return resolved.substring(0, Math.min(resolved.length(), 50));
    }

    private AccountMaster createGoogleAccount(GoogleTokenInfoDto tokenInfo, String userType) {
        try {
            AccountMaster account = new AccountMaster();
            account.setUserName(createGoogleUsername(tokenInfo));
            account.setEmail(tokenInfo.getEmail());
            account.setFirstName(resolveFirstName(tokenInfo));
            account.setLastName(tokenInfo.getFamilyName());
            account.setUserImage(tokenInfo.getPicture());
            account.setPassword(passwordUtil.encrypt(
                    UUID.randomUUID().toString(),
                    encryptionProperties.getKeyConfig(),
                    encryptionProperties.getSalt()));
            account.setPreferLang("en");
            account.setPreferMood("1");
            account.setUserType(resolveUserType(userType));
            account.setActive(true);
            account.setAccessLevel(AccessLevel.USER);

            return accountMasterRepository.save(account);
        } catch (TanaException e) {
            throw e;
        } catch (Exception e) {
            throw new TanaException(CustomCodeErrors.GENERIC_ERROR);
        }
    }

    private String createGoogleUsername(GoogleTokenInfoDto tokenInfo) {
        String email = tokenInfo.getEmail();
        String localPart = email.contains("@") ? email.substring(0, email.indexOf("@")) : "google";
        String base = localPart
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");

        if (base.isBlank()) {
            base = "google";
        }

        String suffix = tokenInfo.getSub();
        if (ObjectUtils.isEmpty(suffix)) {
            suffix = UUID.randomUUID().toString();
        }

        suffix = suffix.replaceAll("[^a-zA-Z0-9]", "");
        String username = base;

        if (!ObjectUtils.isEmpty(accountMasterRepository.findByUserName(username))) {
            int maxBaseLength = Math.min(base.length(), 52);
            username = base.substring(0, maxBaseLength) + "_" + suffix.substring(0, Math.min(suffix.length(), 10));
        }

        return username.length() > 64 ? username.substring(0, 64) : username;
    }

    private String resolveFirstName(GoogleTokenInfoDto tokenInfo) {
        if (!ObjectUtils.isEmpty(tokenInfo.getGivenName())) {
            return tokenInfo.getGivenName();
        }

        if (!ObjectUtils.isEmpty(tokenInfo.getName())) {
            return tokenInfo.getName();
        }

        return tokenInfo.getEmail();
    }

    private Integer resolveUserType(String userType) {
        if (ObjectUtils.isEmpty(userType)) {
            return UserTypeEnum.LOCAL.getValue();
        }

        return UserTypeEnum.fromCode(userType).getValue();
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
