package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;

/** Actualización completa ({@code PUT}); el token, si existe, lo añade el filtro de la sesión. */
public final class UpdateBooking implements Performable {

    private final int bookingId;
    private final Booking booking;

    private UpdateBooking(int bookingId, Booking booking) {
        this.bookingId = bookingId;
        this.booking = booking;
    }

    public static Builder withId(int bookingId) {
        return booking -> new UpdateBooking(bookingId, booking);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        api.record(api.bookings().update(bookingId, booking));
    }

    @Override
    public String description() {
        return "actualiza la reserva %d con %s".formatted(bookingId, booking);
    }

    @FunctionalInterface
    public interface Builder {
        UpdateBooking to(Booking booking);
    }
}
