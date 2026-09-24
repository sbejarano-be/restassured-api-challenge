package com.qa.restfulbooker.api.filters;

import io.restassured.filter.FilterContext;
import io.restassured.filter.OrderedFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import java.util.Set;

/**
 * Inyecta la cookie {@code token} en las peticiones de modificación (PUT, PATCH y DELETE).
 * Cada actor tiene su propia instancia, así que el token nunca se comparte entre hilos.
 */
public final class AuthTokenFilter implements OrderedFilter {

    private static final Set<String> MODIFYING_METHODS = Set.of("PUT", "PATCH", "DELETE");

    private volatile String token;

    public void useToken(String token) {
        this.token = token;
    }

    public void discardToken() {
        this.token = null;
    }

    @Override
    public Response filter(FilterableRequestSpecification request,
                           FilterableResponseSpecification response,
                           FilterContext context) {
        String currentToken = token;
        if (currentToken != null && MODIFYING_METHODS.contains(request.getMethod())) {
            request.cookie("token", currentToken);
        }
        return context.next(request, response);
    }

    /** Se ejecuta antes que el filtro de Allure, para que el reporte muestre la cookie enviada. */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
