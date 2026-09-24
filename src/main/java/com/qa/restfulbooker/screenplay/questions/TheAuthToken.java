package com.qa.restfulbooker.screenplay.questions;

import com.qa.restfulbooker.models.AuthResponse;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Question;

public final class TheAuthToken {

    private TheAuthToken() {
    }

    /** Token de la última autenticación, o {@code null} si /auth no emitió ninguno. */
    public static Question<String> issued() {
        return actor -> actor.abilityTo(CallTheBookerApi.class)
                .lastAuthentication()
                .map(AuthResponse::getToken)
                .orElse(null);
    }
}
