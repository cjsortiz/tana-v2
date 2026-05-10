package com.tana.tana_common.constant;

import lombok.

        Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {

    public String keyConfig;
    public String salt;
}