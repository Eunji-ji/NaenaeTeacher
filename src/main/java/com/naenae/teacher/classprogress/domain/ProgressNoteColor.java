package com.naenae.teacher.classprogress.domain;

import java.util.concurrent.ThreadLocalRandom;

public enum ProgressNoteColor {
    YELLOW("sticky-yellow"),
    LIGHT_BLUE("sticky-blue"),
    LIGHT_GREEN("sticky-green"),
    LIGHT_PINK("sticky-pink");

    private static final ProgressNoteColor[] VALUES = values();
    private final String cssClass;

    ProgressNoteColor(String cssClass) {
        this.cssClass = cssClass;
    }

    public String getCssClass() {
        return cssClass;
    }

    public static ProgressNoteColor random() {
        return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)];
    }
}

