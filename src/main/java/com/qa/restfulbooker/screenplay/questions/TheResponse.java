package com.qa.restfulbooker.screenplay.questions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Question;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import java.util.concurrent.TimeUnit;

/** Preguntas sobre la última respuesta que recibió el actor. */
public final class TheResponse {

    private TheResponse() {
    }

    public static Question<Integer> statusCode() {
        return actor -> lastResponseOf(actor).statusCode();
    }

    public static Question<Headers> headers() {
        return actor -> lastResponseOf(actor).headers();
    }

    public static <T> Question<T> body(Class<T> type) {
        return actor -> lastResponseOf(actor).as(type);
    }

    public static Question<Long> timeInMillis() {
        return actor -> lastResponseOf(actor).timeIn(TimeUnit.MILLISECONDS);
    }

    private static Response lastResponseOf(Actor actor) {
        return actor.abilityTo(CallTheBookerApi.class).lastResponse();
    }
}
