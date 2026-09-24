package com.qa.restfulbooker.screenplay.questions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Question;

public final class TheCreatedBooking {

    private TheCreatedBooking() {
    }

    public static Question<Integer> id() {
        return actor -> actor.abilityTo(CallTheBookerApi.class)
                .lastCreatedBookingId()
                .orElseThrow(() -> new IllegalStateException("%s no ha creado ninguna reserva".formatted(actor)));
    }
}
