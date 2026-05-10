package com.tana.tana_common.constant.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SubCategoryTypeEnum {

    ICONIC("Iconic"),
    COASTAL("Coastal"),
    LOCAL_LIFE("Local Life"),
    FESTIVAL("Festival"),
    CAFES("Cafes"),
    COFFEE("Coffee"),
    SOCIAL("Social"),
    PERSONAL("Personal"),
    OUTDOOR("Outdoor"),
    BREEZY("Breezy"),
    UNHURRIED("Unhurried"),
    HISTORY("History"),
    LOCAL("Local"),
    COMFORT("Comfort"),
    TEAM("Team"),
    SELF_CARE("Self-care"),
    DIP("Dip"),
    SCENIC("Scenic"),
    CREATE("Create"),
    RELIGION("Religion"),
    FUSION("Fusion"),
    RHYTHM("Rhythm"),
    VIBE("Vibe"),
    SLOW("Slow"),
    NOMAD("Nomad"),
    ARCHITECTURE("Architecture"),
    WARM("Warm"),
    HERITAGE("Heritage"),
    QUIET("Quiet"),
    REFLECTIVE("Reflective"),
    WANDER("Wander"),
    SWEET("Sweet"),
    OPEN_AIR("Open-air"),
    AUTHENTIC("Authentic");

    private final String subTypeString;

    SubCategoryTypeEnum(String subTypeString){
        this.subTypeString = subTypeString;
    }

    @Override
    public String toString() {
        return subTypeString;
    }

    public static SubCategoryTypeEnum fromString(String value) {
        return Arrays.stream(SubCategoryTypeEnum.values())
                .filter(e ->
                        e.subTypeString.equalsIgnoreCase(value)
                                || e.name().equalsIgnoreCase(value)
                                || e.name().replace("_", " ").equalsIgnoreCase(value)
                )
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid sub category: " + value));
    }
}
