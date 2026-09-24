package com.qa.restfulbooker.api.specs;

import com.qa.restfulbooker.api.filters.AuthTokenFilter;
import com.qa.restfulbooker.config.Environment;
import com.qa.restfulbooker.support.JacksonMapper;
import io.qameta.allure.Allure;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Especificaciones de petición reutilizables. No se toca la configuración estática de Rest Assured
 * ({@code RestAssured.baseURI} y similares) porque no es segura con pruebas en paralelo.
 */
public final class RequestSpecs {

    private static final String APPLICATION_JSON = "application/json";
    private static final int CONNECTION_TIMEOUT_MS = 10_000;
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    private RequestSpecs() {
    }

    /**
     * URI del ambiente, JSON, timeouts, token de la sesión y petición/respuesta adjuntas en Allure.
     * El Accept es exactamente {@code application/json}: la API responde 418 a listas de tipos (DEF-05),
     * y las pruebas de los demás escenarios no deben depender de ese defecto.
     */
    public static RequestSpecification bookerApi(Environment environment, AuthTokenFilter authTokenFilter) {
        return new RequestSpecBuilder()
                .setBaseUri(environment.baseUri())
                .setContentType(ContentType.JSON)
                .setAccept(APPLICATION_JSON)
                .setConfig(config(httpClientConfig()))
                .addFilter(authTokenFilter)
                .addFilter(new AllureRestAssured())
                .build();
    }

    /**
     * Configuración para enviar una petición sin header Accept. Rest Assured vuelve a añadir {@code Accept: *}{@code /*}
     * antes de cada filtro y al enviar, así que se elimina en el cliente HTTP. Como el registro de Rest Assured en Allure
     * seguirá mostrando {@code *}{@code /*}, los headers realmente enviados se adjuntan aparte.
     */
    public static RestAssuredConfig withoutAcceptHeader() {
        return config(httpClientConfig().httpClientFactory(RequestSpecs::clientWithoutAcceptHeader));
    }

    /**
     * El log es el equivalente de {@code log().ifValidationFails()} para todas las peticiones: imprime petición
     * y respuesta solo cuando una validación falla. Se configura aquí porque {@code then().spec(...)} ignora el log
     * declarado en {@code given()}.
     */
    private static RestAssuredConfig config(HttpClientConfig httpClientConfig) {
        return RestAssuredConfig.config()
                .logConfig(LogConfig.logConfig()
                        .enableLoggingOfRequestAndResponseIfValidationFails(LogDetail.ALL)
                        .enablePrettyPrinting(true))
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .jackson2ObjectMapperFactory((type, charset) -> JacksonMapper.instance()))
                .httpClient(httpClientConfig);
    }

    private static HttpClientConfig httpClientConfig() {
        return HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", CONNECTION_TIMEOUT_MS)
                .setParam("http.socket.timeout", SOCKET_TIMEOUT_MS);
    }

    /** Rest Assured 6 solo admite clientes {@code AbstractHttpClient} de Apache HttpClient 4, que están obsoletos. */
    @SuppressWarnings("deprecation")
    private static HttpClient clientWithoutAcceptHeader() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.addRequestInterceptor((request, context) -> {
            request.removeHeaders(HttpHeaders.ACCEPT);
            Allure.addAttachment("Headers realmente enviados (sin Accept)", "text/plain",
                    Arrays.stream(request.getAllHeaders()).map(Header::toString).collect(Collectors.joining("\n")));
        });
        return client;
    }
}
