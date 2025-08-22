
package com.girbola.configuration;

enum ThemePath {
    DARK("dark"),
    LIGHT("light");

    private String type;

    public String getType() {
        return type;
    }

    ThemePath(String type) {
        this.type = type;
    }
}
