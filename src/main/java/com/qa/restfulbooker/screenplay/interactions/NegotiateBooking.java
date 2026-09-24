package com.qa.restfulbooker.screenplay.interactions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import java.util.Optional;

/** {@code GET /booking/{id}} controlando la negociación de contenido: sin header Accept o con uno concreto. */
public final class NegotiateBooking implements Performable {

    private final int bookingId;
    private final Optional<String> accept;

    private NegotiateBooking(int bookingId, Optional<String> accept) {
        this.bookingId = bookingId;
        this.accept = accept;
    }

    public static NegotiateBooking withoutAcceptHeader(int bookingId) {
        return new NegotiateBooking(bookingId, Optional.empty());
    }

    public static NegotiateBooking accepting(int bookingId, String mediaTypes) {
        return new NegotiateBooking(bookingId, Optional.of(mediaTypes));
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        api.record(accept
                .map(mediaTypes -> api.bookings().findByIdAccepting(bookingId, mediaTypes))
                .orElseGet(() -> api.bookings().findByIdWithoutAcceptHeader(bookingId)));
    }

    @Override
    public String description() {
        return accept
                .map(mediaTypes -> "consulta la reserva %d con Accept: %s".formatted(bookingId, mediaTypes))
                .orElse("consulta la reserva %d sin header Accept".formatted(bookingId));
    }
}
