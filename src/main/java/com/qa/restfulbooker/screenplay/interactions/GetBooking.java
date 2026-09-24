package com.qa.restfulbooker.screenplay.interactions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;

public final class GetBooking implements Performable {

    private final int bookingId;

    private GetBooking(int bookingId) {
        this.bookingId = bookingId;
    }

    public static GetBooking withId(int bookingId) {
        return new GetBooking(bookingId);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        api.record(api.bookings().findById(bookingId));
    }

    @Override
    public String description() {
        return "consulta la reserva " + bookingId;
    }
}
