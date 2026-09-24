package com.qa.restfulbooker.scenarios;

import static com.qa.restfulbooker.api.specs.ResponseSpecs.created;
import static com.qa.restfulbooker.api.specs.ResponseSpecs.notFound;
import static com.qa.restfulbooker.api.specs.ResponseSpecs.okJson;
import static com.qa.restfulbooker.screenplay.consequences.SeeThatResponse.seeThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

import com.qa.restfulbooker.data.BookingFactory;
import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.screenplay.interactions.GetBooking;
import com.qa.restfulbooker.screenplay.questions.TheAuthToken;
import com.qa.restfulbooker.screenplay.questions.TheCreatedBooking;
import com.qa.restfulbooker.screenplay.questions.TheResponse;
import com.qa.restfulbooker.screenplay.tasks.Authenticate;
import com.qa.restfulbooker.screenplay.tasks.CreateBooking;
import com.qa.restfulbooker.screenplay.tasks.DeleteBooking;
import com.qa.restfulbooker.screenplay.tasks.UpdateBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Feature("E1 · Autenticación dinámica y reutilización de sesión")
@DisplayName("E1 · Autenticación dinámica y reutilización de sesión")
class AuthenticationSessionTest extends BookerApiTest {

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("El token de /auth se reutiliza en el PUT y el DELETE de la misma sesión (crear → actualizar → eliminar → 404)")
    void theIssuedTokenIsReusedAcrossModifyingRequests() {
        receptionist.attemptsTo(Authenticate.withTheEnvironmentCredentials());
        receptionist.should(seeThatResponse("/auth responde 200 con JSON", okJson()));
        assertThat(receptionist.asksFor(TheAuthToken.issued())).as("token emitido por /auth").isNotBlank();

        receptionist.attemptsTo(CreateBooking.with(BookingFactory.randomBooking()));
        receptionist.should(seeThatResponse("la reserva se crea", okJson()));
        int bookingId = receptionist.asksFor(TheCreatedBooking.id());

        Booking changes = BookingFactory.randomBooking();
        receptionist.attemptsTo(UpdateBooking.withId(bookingId).to(changes));
        receptionist.should(seeThatResponse("el PUT con el token de la sesión responde 200", okJson()));
        assertThat(receptionist.asksFor(TheResponse.body(Booking.class)))
                .as("reserva actualizada")
                .usingRecursiveComparison()
                .isEqualTo(changes);

        receptionist.attemptsTo(DeleteBooking.withId(bookingId));
        receptionist.should(seeThatResponse("el DELETE con el mismo token responde 201", created()));

        receptionist.attemptsTo(GetBooking.withId(bookingId));
        receptionist.should(seeThatResponse("la reserva eliminada ya no existe", notFound()));
    }
}
