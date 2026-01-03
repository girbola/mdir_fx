
package com.girbola.controllers.datefixer;

public enum CssStylesEnum {

    BAD_STYLE(Styles.BAD_BACKGROUND_COLOR),
    CONFIRMED_STYLE(Styles.CONFIRMED_BACKGROUND_COLOR),
    GOOD_STYLE(Styles.GOOD_BACKGROUND_COLOR),
    MODIFIED_STYLE(Styles.MODIFIED_BACKGROUND_COLOR),
    SUGGESTED_STYLE(Styles.SUGGESTED_BACKGROUND_STYLE);


    private final String style;

    CssStylesEnum(String style) {
        this.style = style;
    }

    public String getStyle() {
        return style;
    }

    private static class Styles {
        private static final String BAD_BACKGROUND_COLOR = "-fx-background-color: #AA4F4F";
        private static final String CONFIRMED_BACKGROUND_COLOR = "-fx-background-color: #42B242";
        private static final String GOOD_BACKGROUND_COLOR = "-fx-background-color: transparent;";
        private static final String MODIFIED_BACKGROUND_COLOR = "-fx-background-color: #5151A5;";
        private static final String SUGGESTED_BACKGROUND_STYLE = "-fx-background-color: #A3AB4D; -fx-text-fill: #000000;";
    }

}
