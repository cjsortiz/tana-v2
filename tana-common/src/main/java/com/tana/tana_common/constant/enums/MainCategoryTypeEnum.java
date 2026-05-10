package com.tana.tana_common.constant.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum MainCategoryTypeEnum {

    NATURE("Nature"),
    SCENERY("Scenery"),
    COMMUNITY("Community"),
    CULTURE("Culture"),
    FOOD("Food"),
    DRINK("Drink"),
    SPORT("Sports"),
    WELLNESS("Wellness");

    private final String typeString;

    MainCategoryTypeEnum(String typeString) {
        this.typeString = typeString;
    }

    @Override
    public String toString() {
        return typeString;
    }

    public static MainCategoryTypeEnum fromString(String value) {
        return Arrays.stream(MainCategoryTypeEnum.values())
                .filter(e -> e.typeString.equalsIgnoreCase(value)
                        || e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + value));
    }

}

