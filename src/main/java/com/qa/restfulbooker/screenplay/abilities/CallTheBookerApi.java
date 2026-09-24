package com.qa.restfulbooker.screenplay.abilities;

import com.qa.restfulbooker.api.clients.AuthApi;
import com.qa.restfulbooker.api.clients.BookingApi;
import com.qa.restfulbooker.api.clients.HealthApi;
import com.qa.restfulbooker.api.filters.AuthTokenFilter;
import com.qa.restfulbooker.api.specs.RequestSpecs;
import com.qa.restfulbooker.config.Environment;
import com.qa.restfulbooker.models.AuthResponse;
import com.qa.restfulbooker.screenplay.core.Ability;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Habilidad de llamar a Restful-Booker. Guarda el estado de la sesión del actor: token, última respuesta
 * y reservas creadas (para eliminarlas al terminar la prueba).
 */
public final class CallTheBookerApi implements Ability {

    private final Environment environment;
    private final AuthTokenFilter authTokenFilter = new AuthTokenFilter();
    private final AuthApi authApi;
    private final BookingApi bookingApi;
    private final HealthApi healthApi;
    private final List<Integer> createdBookingIds = new CopyOnWriteArrayList<>();
    private volatile Response lastResponse;
    private volatile AuthResponse lastAuthentication;
    private volatile Integer lastCreatedBookingId;

    private CallTheBookerApi(Environment environment) {
        this.environment = environment;
        RequestSpecification spec = RequestSpecs.bookerApi(environment, authTokenFilter);
        this.authApi = new AuthApi(spec);
        this.bookingApi = new BookingApi(spec);
        this.healthApi = new HealthApi(spec);
    }

    public static CallTheBookerApi at(Environment environment) {
        return new CallTheBookerApi(environment);
    }

    public Environment environment() {
        return environment;
    }

    public AuthApi auth() {
        return authApi;
    }

    public BookingApi bookings() {
        return bookingApi;
    }

    public HealthApi health() {
        return healthApi;
    }

    public void useToken(String token) {
        authTokenFilter.useToken(token);
    }

    public Response record(Response response) {
        lastResponse = response;
        return response;
    }

    public Response lastResponse() {
        if (lastResponse == null) {
            throw new IllegalStateException("El actor todavía no ha hecho ninguna petición");
        }
        return lastResponse;
    }

    public void rememberAuthentication(AuthResponse authentication) {
        lastAuthentication = authentication;
    }

    public Optional<AuthResponse> lastAuthentication() {
        return Optional.ofNullable(lastAuthentication);
    }

    public void rememberCreatedBooking(int bookingId) {
        createdBookingIds.add(bookingId);
        lastCreatedBookingId = bookingId;
    }

    public Optional<Integer> lastCreatedBookingId() {
        return Optional.ofNullable(lastCreatedBookingId);
    }

    public void forgetBooking(int bookingId) {
        createdBookingIds.remove(Integer.valueOf(bookingId));
    }

    public List<Integer> createdBookingIds() {
        return List.copyOf(createdBookingIds);
    }
}
