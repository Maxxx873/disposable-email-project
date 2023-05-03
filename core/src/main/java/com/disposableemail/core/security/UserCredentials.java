package com.disposableemail.core.security;

import lombok.Builder;
import lombok.Data;
import org.springframework.security.oauth2.jwt.Jwt;

@Data
@Builder
public class UserCredentials {

    private String sub;
    private String preferredUsername;

    public static UserCredentials convert(Jwt jwt) {

        return UserCredentials.builder()
                .sub(jwt.getClaimAsString("sub"))
                .preferredUsername(jwt.getClaimAsString("preferred_username"))
                .build();
    }
}
