package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.config.Credentials;
import com.qa.restfulbooker.models.AuthRequest;
import com.qa.restfulbooker.models.AuthResponse;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import io.restassured.response.Response;
import java.util.Optional;

/**
 * Obtiene un token en {@code /auth}, deserializa la respuesta a {@link AuthResponse} y, si hay token,
 * lo deja listo para que el filtro de la sesión lo envíe en los PUT y DELETE siguientes.
 */
public final class Authenticate implements Performable {

    private final Optional<Credentials> credentials;

    private Authenticate(Optional<Credentials> credentials) {
        this.credentials = credentials;
    }

    public static Authenticate withTheEnvironmentCredentials() {
        return new Authenticate(Optional.empty());
    }

    public static Authenticate withCredentials(Credentials credentials) {
        return new Authenticate(Optional.of(credentials));
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        Credentials effective = credentials.orElseGet(() -> api.environment().credentials());
        Response response = api.record(api.auth().createToken(AuthRequest.from(effective)));

        AuthResponse authentication = response.as(AuthResponse.class);
        api.rememberAuthentication(authentication);
        Optional.ofNullable(authentication.getToken())
                .filter(token -> !token.isBlank())
                .ifPresent(api::useToken);
    }

    @Override
    public String description() {
        return credentials
                .map(value -> "se autentica como '%s'".formatted(value.username()))
                .orElse("se autentica con las credenciales del ambiente");
    }
}
