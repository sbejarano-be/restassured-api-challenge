package com.qa.restfulbooker.screenplay.core;

/** Información que el actor consulta sobre el estado del sistema, sin modificarlo. */
@FunctionalInterface
public interface Question<T> {

    T answeredBy(Actor actor);
}
