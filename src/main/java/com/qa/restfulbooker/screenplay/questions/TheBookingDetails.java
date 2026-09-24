package com.qa.restfulbooker.screenplay.questions;

import com.qa.restfulbooker.api.clients.BookingApi;
import com.qa.restfulbooker.api.specs.ResponseSpecs;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.core.Question;
import com.qa.restfulbooker.support.JacksonMapper;
import io.restassured.common.mapper.TypeRef;
import io.restassured.path.json.JsonPath;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * El listado de la API solo trae IDs, así que cada ID se completa con su detalle y se arma
 * la colección {@code [{bookingid, ...detalle}]}, sobre la que luego se aplican expresiones GPath.
 */
public final class TheBookingDetails implements Question<JsonPath> {

    private final List<Integer> bookingIds;

    private TheBookingDetails(List<Integer> bookingIds) {
        this.bookingIds = List.copyOf(bookingIds);
    }

    public static TheBookingDetails of(List<Integer> bookingIds) {
        return new TheBookingDetails(bookingIds);
    }

    @Override
    public JsonPath answeredBy(Actor actor) {
        BookingApi bookings = actor.abilityTo(CallTheBookerApi.class).bookings();
        List<Map<String, Object>> details = bookingIds.stream()
                .map(bookingId -> detailOf(bookings, bookingId))
                .toList();
        return JsonPath.from(JacksonMapper.toJson(details));
    }

    private static Map<String, Object> detailOf(BookingApi bookings, int bookingId) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("bookingid", bookingId);
        detail.putAll(bookings.findById(bookingId)
                .then().spec(ResponseSpecs.okJson())
                .extract().as(new TypeRef<Map<String, Object>>() {
                }));
        return detail;
    }
}
