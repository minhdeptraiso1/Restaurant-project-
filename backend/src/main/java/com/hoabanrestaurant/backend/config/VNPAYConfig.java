package com.hoabanrestaurant.backend.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment.vn-pay")
@Getter
public class VNPAYConfig {
    private String url;
    private String tmnCode;
    private String secretKey;
    private String returnUrl;
    private String ipnUrl;     // thêm ipnUrl
    private String version;
    private String command;
    private String orderType;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
