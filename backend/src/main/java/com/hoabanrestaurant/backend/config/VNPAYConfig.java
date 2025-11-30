package com.hoabanrestaurant.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment.vn-pay")
public class VNPAYConfig {
    private String vnpUrl;
    private String tmnCode;
    private String secretKey;
    private String returnUrl;
    private String ipnUrl;
    private String version;
    private String command;
    private String orderType;
}
