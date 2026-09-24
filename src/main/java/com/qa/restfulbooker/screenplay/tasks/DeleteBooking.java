package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import io.restassured.response.Response;

public final class DeleteBooking implements Performable {

    /** Código que la API documenta para una eliminación exitosa. */
    private static final int DELETED = 201;

    private final int bookingId;

    private DeleteBooking(int bookingId) {
        this.bookingId = bookingId;
    }

    public static DeleteBooking withId(int bookingId) {
        return new DeleteBooking(bookingId);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        Response response = api.record(api.bookings().delete(bookingId));
        if (response.statusCode() == DELETED) {
            api.forgetBooking(bookingId);
        }
    }

    @Override
    public String description() {
        return "elimina la reserva " + bookingId;
    }
}
