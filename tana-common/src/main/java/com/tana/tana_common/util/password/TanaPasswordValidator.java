package com.tana.tana_common.util.password;

import org.passay.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TanaPasswordValidator {

    @Bean
    public PasswordValidator getPasswordValidator() {
        return new PasswordValidator(
                // length between 10 and 25 characters
                new LengthRule(10, 25),

                // at least one upper-case character
                new CharacterRule(EnglishCharacterData.UpperCase, 1),

                // at least one digit character
                new CharacterRule(EnglishCharacterData.Digit, 1),

                // at least one symbol (special character)
                new CharacterRule(EnglishCharacterData.Special, 1),

                // no whitespace
                new WhitespaceRule());
    }
}
