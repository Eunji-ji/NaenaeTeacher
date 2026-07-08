package com.naenae.common.vocabulary.domain;

public enum WordLevel {
    LOWER_ELEMENTARY("초등저학년"),
    UPPER_ELEMENTARY("초등고학년"),
    MIDDLE_SCHOOL("중학생");

    private final String label;

    WordLevel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}