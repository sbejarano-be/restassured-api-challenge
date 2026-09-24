package com.qa.restfulbooker.scenarios;

import static com.qa.restfulbooker.api.specs.ResponseSpecs.okJson;
import static com.qa.restfulbooker.screenplay.consequences.SeeThatResponse.seeThatResponse;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;

import com.qa.restfulbooker.data.BookingFactory;
import com.qa.restfulbooker.screenplay.interactions.GetBooking;
import com.qa.restfulbooker.screenplay.interactions.PingTheService;
import com.qa.restfulbooker.screenplay.questions.TheCreatedBooking;
import com.qa.restfulbooker.screenplay.questions.TheResponse;
import com.qa.restfulbooker.screenplay.tasks.CreateBooking;
import io.qameta.allure.Allure;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.http.Headers;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Feature("E4 · Validaciones avanzadas: contrato, SLA y headers")
@DisplayName("E4 · Validaciones avanzadas: contrato, SLA y headers")
class AdvancedValidationsTest extends BookerApiTest {

    private int bookingId;

    @BeforeEach
    void createTheBookingUnderTest() {
        receptionist.attemptsTo(CreateBooking.with(BookingFactory.randomBooking()));
        receptionist.should(seeThatResponse("la reserva de prueba se crea", okJson()));
        bookingId = receptionist.asksFor(TheCreatedBooking.id());
    }

    @Test
    @Tag(Tags.CONTRACT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("GET /booking/{id} cumple estrictamente el JSON Schema del contrato")
    void theBookingMatchesTheContractSchema() {
        receptionist.attemptsTo(GetBooking.withId(bookingId));

        receptionist.should(seeThatResponse("la respuesta cumple schemas/booking-schema.json", response -> response
                .spec(okJson())
                .body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))));
    }

    @Test
    @Tag(Tags.PERFORMANCE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("GET /booking/{id} responde dentro del SLA (2000 ms por defecto)")
    void theBookingIsServedWithinTheSla() {
        receptionist.attemptsTo(PingTheService.now());
        Allure.parameter("Calentamiento /ping (ms)", receptionist.asksFor(TheResponse.timeInMillis()));

        receptionist.attemptsTo(GetBooking.withId(bookingId));

        receptionist.should(seeThatResponse("responde en menos de %d ms".formatted(environment.slaMillis()),
                response -> response.time(lessThan(environment.slaMillis()), TimeUnit.MILLISECONDS)));
    }

    @Test
    @Tag(Tags.CONTRACT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("La respuesta declara Content-Type: application/json; charset=utf-8")
    void theResponseDeclaresJsonContentType() {
        receptionist.attemptsTo(GetBooking.withId(bookingId));

        receptionist.should(seeThatResponse("el Content-Type es exacto",
                response -> response.header("Content-Type", "application/json; charset=utf-8")));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("La respuesta incluye el header de servidor")
    void theResponseIncludesTheServerHeader() {
        receptionist.attemptsTo(GetBooking.withId(bookingId));

        receptionist.should(seeThatResponse("el header Server está presente",
                response -> response.header("Server", not(emptyOrNullString()))));
    }

    @Test
    @Tag(Tags.SECURITY)
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("La respuesta incluye los headers de seguridad que OWASP recomienda para APIs REST [DEF-06]")
    void theResponseIncludesTheRecommendedSecurityHeaders() {
        receptionist.attemptsTo(GetBooking.withId(bookingId));

        Headers headers = receptionist.asksFor(TheResponse.headers());
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(headers.hasHeaderWithName("Strict-Transport-Security"))
                    .as("[DEF-06] Strict-Transport-Security presente")
                    .isTrue();
            softly.assertThat(headers.getValue("X-Content-Type-Options"))
                    .as("[DEF-06] X-Content-Type-Options")
                    .isEqualTo("nosniff");
            softly.assertThat(headers.getValue("Cache-Control"))
                    .as("[DEF-06] Cache-Control")
                    .contains("no-store");
            softly.assertThat(headers.hasHeaderWithName("X-Powered-By"))
                    .as("[DEF-06] X-Powered-By ausente (no revela la tecnología del servidor)")
                    .isFalse();
        });
    }
}
