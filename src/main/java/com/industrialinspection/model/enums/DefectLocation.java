package com.industrialinspection.model.enums;

public enum DefectLocation {
    TOP_LEFT("Top Left"),
    TOP_RIGHT("Top Right"),
    CENTER("Center Core"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM_RIGHT("Bottom Right"),
    PERIMETER("Outer Perimeter / Flange"),
    ENTIRE_SURFACE("Entire Surface / Uniform");

    private final String label;

    DefectLocation(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static DefectLocation fromString(String val) {
        if (val == null) return CENTER;
        try {
            return DefectLocation.valueOf(val.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CENTER;
        }
    }
}
