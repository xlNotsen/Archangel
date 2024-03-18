package com.eu.habbo.habbohotel.items;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FurnitureType {
    FLOOR("S"),
    WALL("I"),
    EFFECT("E"),
    BADGE("B"),
    ROBOT("R"),
    HABBO_CLUB("H"),
    PET("P");

    public final String code;

    public static FurnitureType fromString(String code) {
        return switch (code.toUpperCase()) {
            case "S" -> FLOOR;
            case "I" -> WALL;
            case "E" -> EFFECT;
            case "B" -> BADGE;
            case "R" -> ROBOT;
            case "H" -> HABBO_CLUB;
            case "P" -> PET;
            default -> FLOOR;
        };

    }
}