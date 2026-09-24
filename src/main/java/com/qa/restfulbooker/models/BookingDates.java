package com.qa.restfulbooker.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class BookingDates {

    @JsonProperty("checkin")
    LocalDate checkIn;

    @JsonProperty("checkout")
    LocalDate checkOut;
}
