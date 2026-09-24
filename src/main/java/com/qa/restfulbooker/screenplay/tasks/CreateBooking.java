package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import io.restassured.response.Response;
import java.util.Optional;

public final class CreateBooking implements Performable {

    private final Booking booking;

    private CreateBooking(Booking booking) {
        this.booking = booking;
    }

    public static CreateBooking with(Booking booking) {
        return new CreateBooking(booking);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        Response response = api.record(api.bookings().create(booking));
        createdBookingId(response).ifPresent(api::rememberCreatedBooking);
    }

    /** Si la API aceptó la reserva (aunque no debiera), se registra para eliminarla al final de la prueba. */
    private static Optional<Integer> createdBookingId(Response response) {
        boolean accepted = response.statusCode() == 200 && response.contentType().startsWith("application/json");
        return accepted ? Optional.ofNullable(response.path("bookingid")) : Optional.empty();
    }

    @Override
    public String description() {
        return "crea una reserva con " + booking;
    }
}
