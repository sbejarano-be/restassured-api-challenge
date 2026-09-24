package com.qa.restfulbooker.scenarios;

import static com.qa.restfulbooker.api.specs.ResponseSpecs.okJson;
import static com.qa.restfulbooker.screenplay.consequences.SeeThatResponse.seeThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

import com.qa.restfulbooker.data.BookingFactory;
import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.models.BookingResponse;
import com.qa.restfulbooker.screenplay.interactions.GetBooking;
import com.qa.restfulbooker.screenplay.questions.TheCreatedBooking;
import com.qa.restfulbooker.screenplay.questions.TheResponse;
import com.qa.restfulbooker.screenplay.tasks.CreateBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("E2 · Payload dinámico con Builder Pattern")
@DisplayName("E2 · Payload dinámico con Builder Pattern")
class DynamicPayloadTest extends BookerApiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("La reserva creada con datos dinámicos devuelve exactamente los datos enviados")
    void theCreatedBookingEchoesTheSentData() {
        Booking booking = BookingFactory.randomBooking();

        receptionist.attemptsTo(CreateBooking.with(booking));

        receptionist.should(seeThatResponse("la creación responde 200 con JSON", okJson()));
        BookingResponse response = receptionist.asksFor(TheResponse.body(BookingResponse.class));
        assertThat(response.getBookingId()).as("bookingid asignado").isPositive();
        assertThat(response.getBooking()).as("reserva devuelta").usingRecursiveComparison().isEqualTo(booking);
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("La reserva creada queda guardada con los mismos datos")
    void theCreatedBookingIsPersisted() {
        Booking booking = BookingFactory.randomBooking();
        receptionist.attemptsTo(CreateBooking.with(booking));
        receptionist.should(seeThatResponse("la creación responde 200 con JSON", okJson()));

        receptionist.attemptsTo(GetBooking.withId(receptionist.asksFor(TheCreatedBooking.id())));

        receptionist.should(seeThatResponse("la reserva se puede consultar", okJson()));
        assertThat(receptionist.asksFor(TheResponse.body(Booking.class)))
                .as("reserva guardada")
                .usingRecursiveComparison()
                .isEqualTo(booking);
    }
}
