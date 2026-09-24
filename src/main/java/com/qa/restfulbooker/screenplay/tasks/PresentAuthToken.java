package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;

/** Usa un token dado, sin pasar por {@code /auth}. Sirve para los casos con tokens inválidos. */
public final class PresentAuthToken implements Performable {

    private final String token;

    private PresentAuthToken(String token) {
        this.token = token;
    }

    public static PresentAuthToken withValue(String token) {
        return new PresentAuthToken(token);
    }

    @Override
    public void performAs(Actor actor) {
        actor.abilityTo(CallTheBookerApi.class).useToken(token);
    }

    @Override
    public String description() {
        return "presenta el token '%s'".formatted(token);
    }
}
