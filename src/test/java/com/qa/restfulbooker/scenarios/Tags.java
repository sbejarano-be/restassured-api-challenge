package com.qa.restfulbooker.scenarios;

/** Etiquetas de JUnit para ejecutar subconjuntos, por ejemplo {@code mvn clean test -Dgroups=security}. */
final class Tags {

    static final String CONTRACT = "contract";
    static final String SECURITY = "security";
    static final String PERFORMANCE = "performance";
    static final String NEGATIVE = "negative";
    static final String KNOWN_DEFECT = "known-defect";

    private Tags() {
    }
}
