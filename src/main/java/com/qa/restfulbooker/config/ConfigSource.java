package com.qa.restfulbooker.config;

import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

/** Resuelve cada clave con esta prioridad: propiedad de sistema, variable de entorno y archivo del ambiente. */
final class ConfigSource {

    private final Properties file;

    ConfigSource(Properties file) {
        this.file = file;
    }

    String get(String key) {
        return Stream.of(System.getProperty(key), System.getenv(environmentVariableFor(key)), file.getProperty(key))
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Falta '%s': defínela en el archivo del ambiente o en la variable %s"
                                .formatted(key, environmentVariableFor(key))));
    }

    /** {@code booker.base-uri} se lee de la variable {@code BOOKER_BASE_URI}. */
    static String environmentVariableFor(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }
}
