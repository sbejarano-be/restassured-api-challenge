package com.qa.restfulbooker.screenplay.core;

import io.qameta.allure.Allure;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quien ejecuta las tareas. Cada acción y cada verificación queda como un paso en el reporte de Allure.
 * La API imita la de Serenity Screenplay (whoCan, attemptsTo, asksFor, should) para facilitar una migración.
 */
public final class Actor {

    private final String name;
    private final Map<Class<? extends Ability>, Ability> abilities = new ConcurrentHashMap<>();

    private Actor(String name) {
        this.name = name;
    }

    public static Actor named(String name) {
        return new Actor(name);
    }

    public Actor whoCan(Ability... newAbilities) {
        Arrays.stream(newAbilities).forEach(ability -> abilities.put(ability.getClass(), ability));
        return this;
    }

    public <T extends Ability> T abilityTo(Class<T> abilityType) {
        return Optional.ofNullable(abilities.get(abilityType))
                .map(abilityType::cast)
                .orElseThrow(() -> new MissingAbilityException(name, abilityType));
    }

    public void attemptsTo(Performable... performables) {
        Arrays.stream(performables).forEach(performable ->
                Allure.step(name + " " + performable.description(), () -> performable.performAs(this)));
    }

    public <T> T asksFor(Question<T> question) {
        return question.answeredBy(this);
    }

    public void should(Consequence... consequences) {
        Arrays.stream(consequences).forEach(consequence ->
                Allure.step("%s comprueba que %s".formatted(name, consequence.description()),
                        () -> consequence.evaluateFor(this)));
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
