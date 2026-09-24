package com.qa.restfulbooker.screenplay.core;

/** Algo que un actor hace: una tarea de negocio o una interacción con la API. */
public interface Performable {

    void performAs(Actor actor);

    /** Frase en tercera persona que aparece como paso del reporte, por ejemplo "crea una reserva". */
    String description();
}
