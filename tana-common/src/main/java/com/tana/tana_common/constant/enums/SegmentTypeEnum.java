package com.tana.tana_common.constant.enums;

import lombok.Getter;

@Getter
public enum SegmentTypeEnum {
    NATURE_AND_SCENERY("Nature & Scenery"),
    COMMUNITY_AND_CULTURE("Community & Culture"),
    FOOD_AND_DRINK("Food & Drink"),
    SPORTS_AND_WELLNESS("Sports & Wellness");

    private final String label;

    SegmentTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
