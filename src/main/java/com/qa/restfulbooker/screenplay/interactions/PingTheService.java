package com.qa.restfulbooker.screenplay.interactions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;

public final class PingTheService implements Performable {

    private PingTheService() {
    }

    public static PingTheService now() {
        return new PingTheService();
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        api.record(api.health().ping());
    }

    @Override
    public String description() {
        return "comprueba que el servicio responde (/ping)";
    }
}
