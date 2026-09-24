package com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/** Respuesta de {@code POST /booking}: el ID asignado y la reserva guardada. */
@Value
@Builder
@Jacksonized
public class BookingResponse {

    @JsonProperty("bookingid")
    Integer bookingId;

    Booking booking;
}
