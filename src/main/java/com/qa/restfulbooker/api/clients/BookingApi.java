package com.qa.restfulbooker.api.clients;

import static io.restassured.RestAssured.given;

import com.qa.restfulbooker.api.Endpoints;
import com.qa.restfulbooker.api.specs.RequestSpecs;
import com.qa.restfulbooker.models.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

/** API Object de {@code /booking}: encapsula rutas, métodos HTTP y parámetros de cada operación. */
public final class BookingApi {

    private final RequestSpecification spec;

    public BookingApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response create(Booking booking) {
        return request().body(booking).post(Endpoints.BOOKINGS);
    }

    public Response findById(int bookingId) {
        return request().pathParam("id", bookingId).get(Endpoints.BOOKING_BY_ID);
    }

    public Response findByIdWithoutAcceptHeader(int bookingId) {
        return request()
                .config(RequestSpecs.withoutAcceptHeader())
                .pathParam("id", bookingId)
                .get(Endpoints.BOOKING_BY_ID);
    }

    public Response findByIdAccepting(int bookingId, String mediaTypes) {
        return request()
                .accept(mediaTypes)
                .pathParam("id", bookingId)
                .get(Endpoints.BOOKING_BY_ID);
    }

    public Response findIds(Map<String, ?> queryParams) {
        return request().queryParams(queryParams).get(Endpoints.BOOKINGS);
    }

    public Response update(int bookingId, Booking booking) {
        return request().pathParam("id", bookingId).body(booking).put(Endpoints.BOOKING_BY_ID);
    }

    public Response delete(int bookingId) {
        return request().pathParam("id", bookingId).delete(Endpoints.BOOKING_BY_ID);
    }

    private RequestSpecification request() {
        return given().spec(spec);
    }
}
