package com.qa.restfulbooker.screenplay.core;

public class MissingAbilityException extends IllegalStateException {

    public MissingAbilityException(String actorName, Class<?> abilityType) {
        super("%s no tiene la habilidad %s".formatted(actorName, abilityType.getSimpleName()));
    }
}
