package com.qa.restfulbooker.models;

import com.qa.restfulbooker.config.Credentials;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AuthRequest {

    String username;

    @ToString.Exclude
    String password;

    public static AuthRequest from(Credentials credentials) {
        return AuthRequest.builder()
                .username(credentials.username())
                .password(credentials.password())
                .build();
    }
}
