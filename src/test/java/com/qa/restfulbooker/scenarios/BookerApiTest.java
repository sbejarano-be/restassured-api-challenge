package com.qa.restfulbooker.scenarios;

import com.qa.restfulbooker.config.Environment;
import com.qa.restfulbooker.screenplay.abilities.CallTheBookerApi;
import com.qa.restfulbooker.screenplay.core.Actor;
import com.qa.restfulbooker.screenplay.tasks.CleanUpCreatedBookings;
import io.qameta.allure.Epic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Cada prueba tiene su propio actor (y por lo tanto su propia sesión), crea sus datos y los elimina al terminar.
 * Así las pruebas son independientes y pueden ejecutarse en paralelo.
 */
@Epic("Restful-Booker API")
abstract class BookerApiTest {

    protected final Environment environment = Environment.current();
    protected Actor receptionist;

    @BeforeEach
    void prepareTheActor() {
        receptionist = Actor.named("Recepcionista").whoCan(CallTheBookerApi.at(environment));
    }

    @AfterEach
    void removeTheBookingsCreatedByTheTest() {
        receptionist.attemptsTo(CleanUpCreatedBookings.ofTheTest());
    }
}
