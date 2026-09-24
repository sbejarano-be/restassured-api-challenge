package com.qa.restfulbooker.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Configuración del ambiente activo. Se elige con {@code -Denv=<ambiente>} ({@code qa} por defecto)
 * y cada valor se puede sobrescribir con una propiedad de sistema o una variable de entorno.
 */
public record Environment(
        String name,
        String baseUri,
        Credentials credentials,
        int priceThreshold,
        long slaMillis,
        int nonexistentBookingId) {

    private static final String DEFAULT_ENVIRONMENT = "qa";
    private static final Environment CURRENT = load(System.getProperty("env", DEFAULT_ENVIRONMENT));

    public static Environment current() {
        return CURRENT;
    }

    static Environment load(String name) {
        ConfigSource config = new ConfigSource(readFile(name));
        return new Environment(
                name,
                config.get("booker.base-uri"),
                new Credentials(config.get("booker.username"), config.get("booker.password")),
                Integer.parseInt(config.get("booker.price-threshold")),
                Long.parseLong(config.get("booker.sla-millis")),
                Integer.parseInt(config.get("booker.nonexistent-booking-id")));
    }

    private static Properties readFile(String name) {
        String path = "config/%s.properties".formatted(name);
        try (InputStream file = Environment.class.getClassLoader().getResourceAsStream(path)) {
            if (file == null) {
                throw new IllegalStateException(
                        "No existe el ambiente '%s': falta src/main/resources/%s".formatted(name, path));
            }
            Properties properties = new Properties();
            properties.load(new InputStreamReader(file, StandardCharsets.UTF_8));
            return properties;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer " + path, e);
        }
    }
}
