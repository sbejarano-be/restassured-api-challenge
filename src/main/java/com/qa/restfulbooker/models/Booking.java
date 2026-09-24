package com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Reserva tal como la define la API. Los campos nulos no se serializan, así que el mismo
 * builder sirve para construir payloads incompletos en los casos negativos.
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Booking {

    @JsonProperty("firstname")
    String firstName;

    @JsonProperty("lastname")
    String lastName;

    @JsonProperty("totalprice")
    Integer totalPrice;

    @JsonProperty("depositpaid")
    Boolean depositPaid;

    @JsonProperty("bookingdates")
    BookingDates bookingDates;

    @JsonProperty("additionalneeds")
    String additionalNeeds;
}
