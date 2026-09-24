package com.qa.restfulbooker.screenplay.interactions;

import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import java.util.Map;

/** {@code GET /booking}, con o sin filtros por query params (firstname, lastname, checkin, checkout). */
public final class ListBookingIds implements Performable {

    private final Map<String, ?> filters;

    private ListBookingIds(Map<String, ?> filters) {
        this.filters = filters;
    }

    public static ListBookingIds all() {
        return new ListBookingIds(Map.of());
    }

    public static ListBookingIds filteredBy(Map<String, ?> filters) {
        return new ListBookingIds(filters);
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        api.record(api.bookings().findIds(filters));
    }

    @Override
    public String description() {
        return filters.isEmpty()
                ? "consulta el listado general de reservas"
                : "consulta las reservas filtradas por " + filters;
    }
}
