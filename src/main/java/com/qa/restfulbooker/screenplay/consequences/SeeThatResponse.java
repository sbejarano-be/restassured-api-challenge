package com.qa.restfulbooker.screenplay.consequences;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Consequence;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.ResponseSpecification;
import java.util.function.Consumer;

/**
 * Valida la última respuesta con Rest Assured. Si la validación falla, la configuración de la especificación
 * base imprime la petición y la respuesta completas.
 */
public final class SeeThatResponse implements Consequence {

    private final String description;
    private final Consumer<ValidatableResponse> expectation;

    private SeeThatResponse(String description, Consumer<ValidatableResponse> expectation) {
        this.description = description;
        this.expectation = expectation;
    }

    public static SeeThatResponse seeThatResponse(String description, ResponseSpecification specification) {
        return new SeeThatResponse(description, response -> response.spec(specification));
    }

    public static SeeThatResponse seeThatResponse(String description, Consumer<ValidatableResponse> expectation) {
        return new SeeThatResponse(description, expectation);
    }

    @Override
    public void evaluateFor(Actor actor) {
        expectation.accept(actor.abilityTo(CallTheBookerApi.class).lastResponse().then());
    }

    @Override
    public String description() {
        return description;
    }
}
