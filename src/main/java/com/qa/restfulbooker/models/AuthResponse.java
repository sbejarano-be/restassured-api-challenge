package com.qa.restfulbooker.models;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Respuesta de {@code POST /auth}: trae {@code token} si las credenciales son válidas o {@code reason} si no. */
@Value
@Builder
@Jacksonized
public class AuthResponse {

    String token;

    String reason;
}
