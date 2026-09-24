package com.qa.restfulbooker.api.clients;

import static io.restassured.RestAssured.given;

import com.qa.restfulbooker.api.Endpoints;
import com.qa.restfulbooker.models.AuthRequest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

/** API Object de {@code /auth}. */
public final class AuthApi {

    private final RequestSpecification spec;

    public AuthApi(RequestSpecification spec) {
        this.spec = spec;
    }

    public Response createToken(AuthRequest credentials) {
        return given().spec(spec).body(credentials).post(Endpoints.AUTH);
    }
}
