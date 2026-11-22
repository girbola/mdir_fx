package com.girbola.styles;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ButtonStylesTest {
    @BeforeAll
    static void startJavaFx() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        try {
            Platform.startup(started::countDown);
            if (!started.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX did not start");
            }
        } catch (IllegalStateException alreadyStarted) {
            if (!"Toolkit already initialized".equals(alreadyStarted.getMessage())) {
                throw alreadyStarted;
            }
        }
    }

    @Test
    void sharedStylesApplyToMainDateFixerAndDialogs() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            for (String theme : new String[]{"dark", "light"}) {
                Color background = Color.web(theme.equals("dark") ? "#343a42" : "#ffffff");
                Color hover = Color.web(theme.equals("dark") ? "#414b57" : "#e8f2fb");
                for (String stylesheet : new String[]{"mainStyle.css", "dateFixer.css", "dialogs.css"}) {
                    Button button = new Button("Test");
                    button.getStyleClass().add("bottomButton");
                    HBox root = new HBox(button);
                    root.getStyleClass().add("bottomBar");
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(Objects.requireNonNull(
                            getClass().getResource("/themes/" + theme + "/" + stylesheet)).toExternalForm());
                    root.applyCss();
                    assertEquals(background, button.getBackground().getFills().get(0).getFill(), theme + " " + stylesheet);
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), true);
                    root.applyCss();
                    assertEquals(hover, button.getBackground().getFills().get(0).getFill(), theme + " " + stylesheet + " hover");
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), true);
                    root.applyCss();
                    assertEquals(Color.web(theme.equals("dark") ? "#2a3038" : "#d3e2ef"),
                            button.getBackground().getFills().get(0).getFill(), theme + " " + stylesheet + " armed");
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), false);
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), false);
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);
                    root.applyCss();
                    assertEquals(Color.web(theme.equals("dark") ? "#7ba5ca" : "#438ac1"),
                            button.getBorder().getStrokes().get(0).getTopStroke(), theme + " " + stylesheet + " focused");
                    button.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), false);
                    button.getStyleClass().add("primary");
                    root.applyCss();
                    assertEquals(Color.web("#245f91"), button.getBackground().getFills().get(0).getFill(),
                            theme + " " + stylesheet + " primary");
                    button.getStyleClass().remove("primary");
                    button.getStyleClass().add("destructive");
                    root.applyCss();
                    assertEquals(Color.web("#793b40"), button.getBackground().getFills().get(0).getFill(),
                            theme + " " + stylesheet + " destructive");
                    button.setStyle("-fx-background-color: magenta;");
                    root.applyCss();
                    assertEquals(Color.MAGENTA, button.getBackground().getFills().get(0).getFill(),
                            theme + " " + stylesheet + " custom");
                    button.setDisable(true);
                    root.applyCss();
                    assertEquals(0.48, button.getOpacity(), 0.001, theme + " " + stylesheet + " disabled");
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void screenStylesCanOverrideDefaultAndAddCustomVariants() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            for (String theme : new String[]{"dark", "light"}) {
                Button button = new Button("Test");
                button.getStyleClass().add("bottomButton");
                HBox root = new HBox(button);
                root.getStyleClass().add("bottomBar");
                Scene scene = new Scene(root);
                scene.getStylesheets().add(Objects.requireNonNull(
                        getClass().getResource("/themes/" + theme + "/mainStyle.css")).toExternalForm());
                scene.getStylesheets().add(Objects.requireNonNull(
                        getClass().getResource("/styles/button-overrides.css")).toExternalForm());
                root.applyCss();
                assertEquals(Color.RED, button.getBackground().getFills().get(0).getFill());
                button.getStyleClass().add("custom");
                root.applyCss();
                assertEquals(Color.BLUE, button.getBackground().getFills().get(0).getFill());
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    @Test
    void screenStylesheetsImportTheSharedDefaults() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            for (String theme : new String[]{"dark", "light"}) {
                for (String stylesheet : new String[]{"buttons.css", "mainStyle.css", "dateFixer.css",
                        "dialogs.css", "folderChooser.css", "imageViewer.css", "newViewFoldersWindowStyle.css",
                        "option_pane.css", "operateFiles.css", "videoPlayerStyle.css", "viewignoredlist.css",
                        "vlcPlayer.css"}) {
                    Button button = new Button("Test");
                    HBox root = new HBox(button);
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(Objects.requireNonNull(
                            getClass().getResource("/themes/" + theme + "/" + stylesheet)).toExternalForm());
                    root.applyCss();
                    assertEquals(Color.web(theme.equals("dark") ? "#343a42" : "#ffffff"),
                            button.getBackground().getFills().get(0).getFill(), theme + " " + stylesheet);
                }
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }
}
