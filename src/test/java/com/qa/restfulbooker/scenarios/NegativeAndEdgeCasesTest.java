package com.qa.restfulbooker.scenarios;

import static com.qa.restfulbooker.api.specs.ResponseSpecs.forbidden;
import static com.qa.restfulbooker.api.specs.ResponseSpecs.notFound;
import static com.qa.restfulbooker.api.specs.ResponseSpecs.okJson;
import static com.qa.restfulbooker.screenplay.consequences.SeeThatResponse.seeThatResponse;
import static org.assertj.core.api.Assertions.assertThat;

import com.qa.restfulbooker.config.Credentials;
import com.qa.restfulbooker.data.BookingFactory;
import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.models.BookingDates;
import com.qa.restfulbooker.screenplay.core.Performable;
import com.qa.restfulbooker.screenplay.interactions.GetBooking;
import com.qa.restfulbooker.screenplay.interactions.NegotiateBooking;
import com.qa.restfulbooker.screenplay.questions.TheAuthToken;
import com.qa.restfulbooker.screenplay.questions.TheCreatedBooking;
import com.qa.restfulbooker.screenplay.questions.TheResponse;
import com.qa.restfulbooker.screenplay.tasks.Authenticate;
import com.qa.restfulbooker.screenplay.tasks.CreateBooking;
import com.qa.restfulbooker.screenplay.tasks.DeleteBooking;
import com.qa.restfulbooker.screenplay.tasks.PresentAuthToken;
import com.qa.restfulbooker.screenplay.tasks.UpdateBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Casos negativos pedidos por el enunciado (pasan) y casos límite que documentan defectos verificados
 * de la API (fallan mientras el defecto exista). Los mensajes de estos últimos incluyen el ID del defecto,
 * que Allure usa para clasificarlos como "defectos conocidos del producto".
 */
@Feature("E5 · Manejo negativo y edge cases")
@DisplayName("E5 · Manejo negativo y edge cases")
class NegativeAndEdgeCasesTest extends BookerApiTest {

    @Tag(Tags.NEGATIVE)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Actualizar una reserva sin un token válido responde 403 y no la modifica")
    @ParameterizedTest(name = "PUT {0} responde 403 y no modifica la reserva")
    @MethodSource("tokensThatDoNotAuthorize")
    void updatingWithoutAValidTokenIsForbidden(Optional<String> token) {
        Booking original = BookingFactory.randomBooking();
        receptionist.attemptsTo(CreateBooking.with(original));
        receptionist.should(seeThatResponse("la reserva de prueba se crea", okJson()));
        int bookingId = receptionist.asksFor(TheCreatedBooking.id());

        token.ifPresent(value -> receptionist.attemptsTo(PresentAuthToken.withValue(value)));
        receptionist.attemptsTo(UpdateBooking.withId(bookingId).to(BookingFactory.randomBooking()));
        receptionist.should(seeThatResponse("la actualización se rechaza con 403", forbidden()));

        receptionist.attemptsTo(GetBooking.withId(bookingId));
        receptionist.should(seeThatResponse("la reserva sigue disponible", okJson()));
        assertThat(receptionist.asksFor(TheResponse.body(Booking.class)))
                .as("la reserva no cambió")
                .usingRecursiveComparison()
                .isEqualTo(original);
    }

    static Stream<Arguments> tokensThatDoNotAuthorize() {
        return Stream.of(
                Arguments.of(Named.of("sin token", Optional.empty())),
                Arguments.of(Named.of("con un token inválido", Optional.of(BookingFactory.forgedToken()))));
    }

    @Test
    @Tag(Tags.NEGATIVE)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Consultar una reserva inexistente responde 404")
    void anUnknownBookingIsNotFound() {
        receptionist.attemptsTo(GetBooking.withId(environment.nonexistentBookingId()));

        receptionist.should(seeThatResponse("la API responde 404", notFound()));
    }

    @Test
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Crear una reserva sin campos obligatorios responde 400 [DEF-01]")
    void aBookingWithoutRequiredFieldsIsRejected() {
        receptionist.attemptsTo(CreateBooking.with(
                Booking.builder().firstName(BookingFactory.randomFirstName()).build()));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-01] código de estado al omitir campos obligatorios")
                .isEqualTo(400);
    }

    @Test
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear una reserva con checkout anterior al checkin responde 400 [DEF-02]")
    void aBookingWithCheckoutBeforeCheckinIsRejected() {
        Booking booking = BookingFactory.randomBooking();
        BookingDates inverted = BookingDates.builder()
                .checkIn(booking.getBookingDates().getCheckOut())
                .checkOut(booking.getBookingDates().getCheckIn())
                .build();

        receptionist.attemptsTo(CreateBooking.with(booking.toBuilder().bookingDates(inverted).build()));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-02] código de estado con checkout anterior al checkin")
                .isEqualTo(400);
    }

    @Test
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Crear una reserva con precio negativo (-1, primer valor inválido) responde 400 [DEF-02]")
    void aBookingWithNegativePriceIsRejected() {
        receptionist.attemptsTo(CreateBooking.with(BookingFactory.randomBooking().toBuilder().totalPrice(-1).build()));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-02] código de estado con precio negativo")
                .isEqualTo(400);
    }

    @Test
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Autenticarse con credenciales inválidas no emite token y responde 401 [DEF-03]")
    void invalidCredentialsAreUnauthorized() {
        Credentials invalid = new Credentials(environment.credentials().username(), BookingFactory.randomPassword());

        receptionist.attemptsTo(Authenticate.withCredentials(invalid));

        assertThat(receptionist.asksFor(TheAuthToken.issued())).as("token con credenciales inválidas").isNull();
        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-03] código de estado con credenciales inválidas")
                .isEqualTo(401);
    }

    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Modificar una reserva inexistente con un token válido responde 404 [DEF-04]")
    @ParameterizedTest(name = "{0} sobre una reserva inexistente responde 404 [DEF-04]")
    @MethodSource("modificationsOfAnUnknownBooking")
    void modifyingAnUnknownBookingIsNotFound(IntFunction<Performable> modification) {
        receptionist.attemptsTo(Authenticate.withTheEnvironmentCredentials());
        receptionist.should(seeThatResponse("/auth responde 200 con JSON", okJson()));

        receptionist.attemptsTo(modification.apply(environment.nonexistentBookingId()));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-04] código de estado al modificar una reserva inexistente")
                .isEqualTo(404);
    }

    static Stream<Arguments> modificationsOfAnUnknownBooking() {
        IntFunction<Performable> update = id -> UpdateBooking.withId(id).to(BookingFactory.randomBooking());
        IntFunction<Performable> delete = DeleteBooking::withId;
        return Stream.of(
                Arguments.of(Named.of("PUT", update)),
                Arguments.of(Named.of("DELETE", delete)));
    }

    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("La API negocia el contenido como indica RFC 9110 [DEF-05]")
    @ParameterizedTest(name = "{0} [DEF-05]")
    @MethodSource("contentNegotiations")
    void theApiNegotiatesContentAsRfc9110Expects(IntFunction<Performable> request, List<Integer> acceptableStatuses) {
        receptionist.attemptsTo(CreateBooking.with(BookingFactory.randomBooking()));
        receptionist.should(seeThatResponse("la reserva de prueba se crea", okJson()));

        receptionist.attemptsTo(request.apply(receptionist.asksFor(TheCreatedBooking.id())));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-05] código de estado según la negociación de contenido")
                .isIn(acceptableStatuses);
    }

    static Stream<Arguments> contentNegotiations() {
        IntFunction<Performable> withoutAccept = NegotiateBooking::withoutAcceptHeader;
        IntFunction<Performable> axiosDefault = id -> NegotiateBooking.accepting(id, "application/json, text/plain, */*");
        IntFunction<Performable> unavailableType = id -> NegotiateBooking.accepting(id, "text/html");
        return Stream.of(
                Arguments.of(Named.of("Sin header Accept: responde 200 con la representación por defecto", withoutAccept),
                        List.of(200)),
                Arguments.of(Named.of("Accept con lista de tipos que incluye JSON (valor por defecto de axios): responde 200", axiosDefault),
                        List.of(200)),
                Arguments.of(Named.of("Accept: text/html (tipo no disponible): responde 406 o la representación por defecto", unavailableType),
                        List.of(200, 406)));
    }

    @Test
    @Tag(Tags.KNOWN_DEFECT)
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Crear una reserva sin additionalneeds (obligatorio según la apidoc) responde 400 [DEF-07]")
    void aBookingWithoutAdditionalNeedsIsRejected() {
        receptionist.attemptsTo(CreateBooking.with(
                BookingFactory.randomBooking().toBuilder().additionalNeeds(null).build()));

        assertThat(receptionist.asksFor(TheResponse.statusCode()))
                .as("[DEF-07] código de estado sin additionalneeds")
                .isEqualTo(400);
    }
}
