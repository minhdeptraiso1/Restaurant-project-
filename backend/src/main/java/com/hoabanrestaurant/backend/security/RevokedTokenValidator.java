package com.hoabanrestaurant.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RevokedTokenValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenBlacklistService blacklist;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti != null && blacklist.isRevoked(jti)) {
            return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Token revoked", null));
        }
        return OAuth2TokenValidatorResult.success();
    }
}
