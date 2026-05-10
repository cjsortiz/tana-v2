package com.tana.tana_common.functions.userdetails;

import com.tana.tana_common.functions.dropdown.repository.DropdownRepository;
import com.tana.tana_common.model.AccountMaster;
import com.tana.tana_common.functions.accountmaster.repository.AccountRepository;
import com.tana.tana_common.model.DropdownMaster;
import com.tana.tana_common.util.CommonUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Constant string to optimize data used.
     */
    private static final String USERNAME_NOT_FOUND_MSG = "User not found with username: ";


    /**
     * {@link AccountRepository} Handles all account related queries.
     */
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DropdownRepository dropdownRepository;


    @Value("${app.user-path}")
    private String userPath;


    /**
     * Load the user using their username.
     *
     * @param userName {@link String}
     * @return {@link UserDetails}
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName) {

        AccountMaster user = accountRepository.findByUserName(userName);

        List<DropdownMaster> dropdownMasterList = dropdownRepository.fetchAllDropdown();

        if(ObjectUtils.isEmpty(user)){
            throw new UsernameNotFoundException(USERNAME_NOT_FOUND_MSG + userName);
        }

        StringBuilder image = new StringBuilder();

        if (!ObjectUtils.isEmpty(user.getUserImage())) {
            image.append(CommonUtils.getUploadImage(user.getUserName(), userPath, user.getUserImage()));
        }

        return UserDetailsImpl.build(
                user,
                Collections.singletonList(new SimpleGrantedAuthority(user.getAccessLevel().name())),
                image.toString(),
                dropdownMasterList
                );


    }
}