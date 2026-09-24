package com.qa.restfulbooker.api.clients;

import static io.restassured.RestAssured.given;

import com.qa.restfulbooker.api.Endpoints;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** API Object de {@code /ping}, el health check del servicio. */
public final class HealthApi {

    private final RequestSpecification spec;

    public HealthApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response ping() {
        return given().spec(spec).get(Endpoints.PING);
    }
}
