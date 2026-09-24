package com.qa.restfulbooker.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

/** Expectativas de respuesta reutilizables. Cada llamada crea una instancia nueva para no compartir estado entre hilos. */
public final class ResponseSpecs {

    private ResponseSpecs() {
    }

    public static ResponseSpecification okJson() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .build();
    }

    public static ResponseSpecification created() {
        return new ResponseSpecBuilder().expectStatusCode(201).build();
    }

    public static ResponseSpecification forbidden() {
        return new ResponseSpecBuilder().expectStatusCode(403).build();
    }

    public static ResponseSpecification notFound() {
        return new ResponseSpecBuilder().expectStatusCode(404).build();
    }
}
