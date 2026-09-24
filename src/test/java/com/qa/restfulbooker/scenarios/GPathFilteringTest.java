package com.qa.restfulbooker.scenarios;

import static com.qa.restfulbooker.api.specs.ResponseSpecs.okJson;
import static com.qa.restfulbooker.screenplay.consequences.SeeThatResponse.seeThatResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.qa.restfulbooker.data.BookingFactory;
import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.screenplay.interactions.GetBooking;
import com.qa.restfulbooker.screenplay.interactions.ListBookingIds;
import com.qa.restfulbooker.screenplay.questions.TheBookingDetails;
import com.qa.restfulbooker.screenplay.questions.TheBookingIds;
import com.qa.restfulbooker.screenplay.questions.TheCreatedBooking;
import com.qa.restfulbooker.screenplay.questions.TheResponse;
import com.qa.restfulbooker.screenplay.tasks.CreateBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.path.json.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("E3 · Serialización, deserialización y expresiones GPath")
@DisplayName("E3 · Serialización, deserialización y expresiones GPath")
class GPathFilteringTest extends BookerApiTest {

    @Test
    @Tag(Tags.CONTRACT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("El listado general cumple su contrato: solo expone bookingid, no totalprice")
    void theGeneralListExposesOnlyBookingIds() {
        receptionist.attemptsTo(ListBookingIds.all());

        receptionist.should(seeThatResponse("el listado cumple el schema booking-ids", response -> response
                .spec(okJson())
                .body(matchesJsonSchemaInClasspath("schemas/booking-ids-schema.json"))));
        assertThat(receptionist.asksFor(TheBookingIds.inTheLastResponse())).as("IDs del listado").isNotEmpty();
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GPath filtra las reservas con totalprice mayor al umbral (valores límite: umbral - 1, umbral, umbral + 1)")
    void gPathFiltersTheBookingsAboveThePriceThreshold() {
        int threshold = environment.priceThreshold();
        String firstName = BookingFactory.uniqueFirstName();
        Map<Integer, Integer> bookingIdByPrice = Stream.of(threshold - 1, threshold, threshold + 1)
                .collect(toMap(identity(), price -> createBooking(firstName, price)));

        receptionist.attemptsTo(ListBookingIds.filteredBy(Map.of("firstname", firstName)));
        receptionist.should(seeThatResponse("la búsqueda por firstname responde 200", okJson()));
        List<Integer> seededIds = receptionist.asksFor(TheBookingIds.inTheLastResponse());
        assertThat(seededIds).as("reservas sembradas").containsExactlyInAnyOrderElementsOf(bookingIdByPrice.values());

        JsonPath details = receptionist.asksFor(TheBookingDetails.of(seededIds));
        List<Integer> aboveThreshold = details.getList(
                "findAll { it.totalprice > %d }.bookingid".formatted(threshold), Integer.class);

        assertThat(aboveThreshold)
                .as("solo la reserva con precio umbral + 1 supera el umbral")
                .containsExactly(bookingIdByPrice.get(threshold + 1));

        receptionist.attemptsTo(GetBooking.withId(aboveThreshold.getFirst()));
        receptionist.should(seeThatResponse("la reserva filtrada se puede consultar", okJson()));
        Booking booking = receptionist.asksFor(TheResponse.body(Booking.class));
        assertThat(booking.getTotalPrice()).as("totalprice de la reserva tipada").isGreaterThan(threshold);
        assertThat(booking.getFirstName()).as("firstname de la reserva tipada").isEqualTo(firstName);
    }

    private int createBooking(String firstName, int totalPrice) {
        receptionist.attemptsTo(CreateBooking.with(BookingFactory.bookingFor(firstName, totalPrice)));
        receptionist.should(seeThatResponse("la reserva de %d se crea".formatted(totalPrice), okJson()));
        return receptionist.asksFor(TheCreatedBooking.id());
    }
}
