package com.qa.restfulbooker.config;

public record Credentials(String username, String password) {

    /** Evita que la contraseña aparezca en logs o en el reporte. */
    @Override
    public String toString() {
        return "Credentials[username=%s, password=****]".formatted(username);
    }
}
