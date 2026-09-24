package com.qa.restfulbooker.data;

import com.qa.restfulbooker.models.Booking;
import com.qa.restfulbooker.models.BookingDates;
import java.time.LocalDate;
import java.util.List;
import net.datafaker.Faker;

/** Genera reservas con datos dinámicos (Datafaker + Builder); no hay JSON estáticos en el proyecto. */
public final class BookingFactory {

    /** Faker no es seguro entre hilos: cada hilo de ejecución usa su propia instancia. */
    private static final ThreadLocal<Faker> FAKER = ThreadLocal.withInitial(Faker::new);

    private static final List<String> ADDITIONAL_NEEDS =
            List.of("Breakfast", "Late checkout", "Airport transfer", "Extra bed", "Dinner");

    private BookingFactory() {
    }

    public static Booking randomBooking() {
        Faker faker = FAKER.get();
        LocalDate checkIn = LocalDate.now().plusDays(faker.number().numberBetween(1, 90));
        return Booking.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .totalPrice(faker.number().numberBetween(50, 2_000))
                .depositPaid(faker.bool().bool())
                .bookingDates(BookingDates.builder()
                        .checkIn(checkIn)
                        .checkOut(checkIn.plusDays(faker.number().numberBetween(1, 15)))
                        .build())
                .additionalNeeds(faker.options().nextElement(ADDITIONAL_NEEDS))
                .build();
    }

    public static Booking bookingFor(String firstName, int totalPrice) {
        return randomBooking().toBuilder()
                .firstName(firstName)
                .totalPrice(totalPrice)
                .build();
    }

    /** Nombre único para aislar los datos de una prueba dentro de la API compartida. */
    public static String uniqueFirstName() {
        return "Qa" + FAKER.get().regexify("[a-z]{12}");
    }

    public static String randomFirstName() {
        return FAKER.get().name().firstName();
    }

    public static String randomPassword() {
        return FAKER.get().credentials().password(12, 20);
    }

    /** Token falso con el mismo formato que los reales: 15 caracteres hexadecimales. */
    public static String forgedToken() {
        return FAKER.get().regexify("[a-f0-9]{15}");
    }
}
