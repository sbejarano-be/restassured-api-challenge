package com.qa.restfulbooker.screenplay.core;

/** Resultado esperado que el actor verifica; si no se cumple, lanza un error de aserción. */
public interface Consequence {

    void evaluateFor(Actor actor);

    String description();
}
