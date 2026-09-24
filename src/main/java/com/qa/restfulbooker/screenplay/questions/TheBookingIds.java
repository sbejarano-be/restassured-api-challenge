package com.qa.restfulbooker.screenplay.questions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Question;
import java.util.List;

public final class TheBookingIds {

    private TheBookingIds() {
    }

    /** Extrae con GPath todos los {@code bookingid} del listado, sin recorrerlo con bucles. */
    public static Question<List<Integer>> inTheLastResponse() {
        return actor -> actor.abilityTo(CallTheBookerApi.class)
                .lastResponse()
                .jsonPath()
                .getList("bookingid", Integer.class);
    }
}
