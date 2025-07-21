package com.girbola.controllers.main.enums;

public enum ThemeType {
    LIGHT("light"),
    DARK("dark");

    private final String value;

    ThemeType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}