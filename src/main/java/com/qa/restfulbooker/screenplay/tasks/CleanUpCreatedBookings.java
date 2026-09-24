package com.qa.restfulbooker.screenplay.tasks;

import com.qa.restfulbooker.models.AuthRequest;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Performable;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Elimina las reservas que creó la prueba. Usa un token propio porque la prueba pudo terminar sin sesión
 * o con un token inválido. Un fallo de limpieza se registra en el log, pero no cambia el resultado de la prueba.
 */
public final class CleanUpCreatedBookings implements Performable {

    private static final Logger LOG = LoggerFactory.getLogger(CleanUpCreatedBookings.class);
    private static final int DELETED = 201;

    private CleanUpCreatedBookings() {
    }

    public static CleanUpCreatedBookings ofTheTest() {
        return new CleanUpCreatedBookings();
    }

    @Override
    public void performAs(Actor actor) {
        CallTheBookerApi api = actor.abilityTo(CallTheBookerApi.class);
        List<Integer> bookingIds = api.createdBookingIds();
        if (bookingIds.isEmpty()) {
            return;
        }
        try {
            String token = api.auth()
                    .createToken(AuthRequest.from(api.environment().credentials()))
                    .path("token");
            api.useToken(token);
            bookingIds.forEach(bookingId -> delete(api, bookingId));
        } catch (RuntimeException e) {
            LOG.warn("No se pudieron eliminar las reservas {}: {}", bookingIds, e.getMessage());
        }
    }

    private static void delete(CallTheBookerApi api, int bookingId) {
        int status = api.bookings().delete(bookingId).statusCode();
        if (status == DELETED) {
            api.forgetBooking(bookingId);
        } else {
            LOG.warn("La reserva {} no se eliminó: HTTP {}", bookingId, status);
        }
    }

    @Override
    public String description() {
        return "elimina las reservas que creó durante la prueba";
    }
}
