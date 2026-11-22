package com.girbola;

import java.util.Objects;

public final class ThemeStyles {
    private ThemeStyles() {
    }

    public static String buttons() {
        String path = Main.conf.getThemePath() + "buttons.css";
        return Objects.requireNonNull(Main.class.getResource(path), "Missing button stylesheet: " + path)
                .toExternalForm();
    }
}
