package com.girbola.controllers.loading;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.Window;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

public class LoadingProcessTaskStatic {

    private static final Logger LOGGER = Logger.getLogger(LoadingProcessTaskStatic.class.getName());
    private static final String ERROR_TAG = LoadingProcessTaskStatic.class.getSimpleName();

    // 🌟 STAATTISET MUUTTUJAT KESKITETTYÄ HALLINTAA VARTEN
    private static final ModelLoading modelLoading = new ModelLoading();
    private static Scene loadingScene;
    private static Stage loadingStage;
    private static int activeTasksCount = 0; // Laskuri käynnissä oleville tehtäville

    private static double xOffset;
    private static double yOffset;

    /**
     * Rekisteröi ja käynnistää uuden tehtävän latausikkunassa.
     * Jos ikkuna ei ole auki, se luodaan ja avataan.
     * Jos se on jo auki, uusi tehtävä sidotaan olemassa olevaan ikkunaan.
     */
    public static synchronized void showLoading(Window owner, Task<?> currentTask) {
        if (currentTask == null) {
            Messages.sprintfError("LoadingProcessTaskStatic: currentTask was null!");
            return;
        }

        activeTasksCount++;
        setTask(currentTask);

        if (Platform.isFxApplicationThread()) {
            initAndShowGUI(owner);
        } else {
            Platform.runLater(() -> initAndShowGUI(owner));
        }

        // Kuunnellaan tehtävän valmistumista, jotta osataan sulkea ikkuna oikeaan aikaan
        currentTask.setOnSucceeded(event -> handleTaskFinished());
        currentTask.setOnFailed(event -> handleTaskFinished());
        currentTask.setOnCancelled(event -> handleTaskFinished());
    }

    private static void initAndShowGUI(Window owner) {
        try {
            // Luodaan käyttöliittymä vain kerran, jos sitä ei ole vielä olemassa tai se on hävitetty
            if (loadingStage == null) {
                FXMLLoader loader = loadFXML();
                if (loader == null || loadingScene == null) return;

                LoadingProcessController lpc = loader.getController();
                if (lpc != null) {
                    lpc.init(modelLoading);
                }

                setupLoadingStage(owner);
                updateSceneSwitcher();
            }

            // Sidotaan kontrollit uuteen aktiiviseen tehtävään
            bind();
            showLoadStage();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load GUI", ex);
            Messages.errorSmth(ERROR_TAG, "Failed to load GUI", ex, Misc.getLineNumber(), true);
        }
    }

    private static FXMLLoader loadFXML() throws IOException {
        URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
        if (fxmlLocation == null) {
            Messages.sprintfError("FXML resource not found");
            Platform.exit();
            return null;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent parent = loader.load();

        loadingScene = new Scene(parent);
        setupDragHandlers();
        applyStylesheet();

        return loader;
    }

    private static void setupLoadingStage(Window owner) {
        loadingStage = new Stage();
        loadingStage.setScene(loadingScene);

        loadingStage.setWidth(500);
        loadingStage.setHeight(400);
        loadingStage.setMinWidth(500);
        loadingStage.setMinHeight(400);
        loadingStage.setMaxWidth(500);
        loadingStage.setMaxHeight(400);

        if (owner != null) {
            loadingStage.initOwner(owner);
        }

        loadingStage.setTitle("Loading Process");
        loadingStage.setAlwaysOnTop(true);
        Main.centerWindowDialog(loadingStage);
    }

    private static void applyStylesheet() {
        try {
            String themePath = conf.getThemePath() + MDir_Stylesheets_Constants.LOADINGPROCESS.getType();
            URL cssResource = Main.class.getResource(themePath);
            if (cssResource != null) {
                loadingScene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                Messages.sprintfError("Stylesheet not found: " + themePath);
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to apply stylesheet", ex);
        }
    }

    private static void setupDragHandlers() {
        loadingScene.setOnMousePressed(event -> {
            if (loadingStage != null) {
                xOffset = loadingStage.getX() - event.getScreenX();
                yOffset = loadingStage.getY() - event.getScreenY();
            }
        });

        loadingScene.setOnMouseDragged(event -> {
            if (loadingStage != null) {
                loadingStage.setX(event.getScreenX() + xOffset);
                loadingStage.setY(Math.max(0, event.getScreenY() + yOffset));
            }
        });
    }

    private static void updateSceneSwitcher() {
        if (Main.sceneManager != null) {
            Main.sceneManager.setWindow_loadingprogress(loadingStage);
            Main.sceneManager.setScene_loading(loadingScene);
        } else {
            Messages.sprintfError("LoadingProcess_Task: Main.sceneManager was null!");
            Messages.warningText("LoadingProcess_Task: Main.sceneManager was null!");
        }
    }

    private static void setTask(Task<?> currentTask) {
        if (modelLoading == null || currentTask == null) {
            Messages.sprintfError("LoadingProcess_Task: modelLoading or currentTask was null!");
            return;
        }
        modelLoading.setTask(currentTask);
    }

    public static void setProgress(double value, double max) {
        if (modelLoading.getProgressBar() != null) {
            Platform.runLater(() -> modelLoading.getProgressBar().setProgress(value / max));
        }
    }

    public static void bind() {
        Task<?> task = modelLoading.getTask();
        if (task != null && modelLoading.getProgressBar() != null && modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> {
                // Varmistetaan, että vanha sidonta puretaan ennen uutta
                modelLoading.getProgressBar().progressProperty().unbind();
                modelLoading.getMessages_lbl().textProperty().unbind();

                modelLoading.getProgressBar().progressProperty().bind(task.progressProperty());
                modelLoading.getMessages_lbl().textProperty().bind(task.messageProperty());
            });
        } else {
            Messages.sprintf("Task, Progress Bar, or Label was null in bind()");
        }
    }

    public static void updateTextArea(String message) {
        if (modelLoading.getMessages_txa() != null) {
            Platform.runLater(() -> {
                modelLoading.getMessages_txa().appendText(message + System.lineSeparator());
            });
        }
    }

    public static void unbind() {
        Platform.runLater(() -> {
            if (modelLoading.getProgressBar() != null) {
                modelLoading.getProgressBar().progressProperty().unbind();
            }
            if (modelLoading.getMessages_lbl() != null) {
                modelLoading.getMessages_lbl().textProperty().unbind();
            }
        });
    }

    /**
     * 🌟 PALAUTETTU METODI STAATTISEKSI MUUTETTUNA
     * Tuo latausikkunan näkyviin ruudulle turvallisesti.
     */
    public static void showLoadStage() {
        Stage stage = (Main.sceneManager != null) ? Main.sceneManager.getWindow_loadingprogress() : loadingStage;

        if (stage == null) {
            Messages.errorSmth(ERROR_TAG, "Loading scene has not been initialized.", null, Misc.getLineNumber(), true);
            return;
        }

        if (stage.isShowing()) {
            Messages.sprintf("Window is already showing. Appending task to existing window.");
            return;
        }

        if (modelLoading.getTask() == null && modelLoading.getProgressBar() != null) {
            Messages.sprintf("Task was null! Setting indeterminate progress.");
            Platform.runLater(() -> modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS));
        }

        Platform.runLater(stage::show);
    }

    /**
     * Käsittelee tehtävän valmistumisen. Vähentää laskuria.
     * Kun kaikki tehtävät ovat valmiita, puretaan vain sidonnat,
     * mutta JÄTETÄÄN ikkuna auki, jotta käyttäjä näkee lopputuloksen.
     */
    private static synchronized void handleTaskFinished() {
        activeTasksCount--;

        if (activeTasksCount <= 0) {
            activeTasksCount = 0;
            unbind();

            // Asetetaan käyttöliittymä staattiseen valmis-tilaan
            Platform.runLater(() -> {
                if (modelLoading.getProgressBar() != null) {
                    modelLoading.getProgressBar().progressProperty().unbind();
                    modelLoading.getProgressBar().setProgress(1.0); // Palkki täyteen
                }
                if (modelLoading.getMessages_lbl() != null) {
                    modelLoading.getMessages_lbl().textProperty().unbind();
                    modelLoading.getMessages_lbl().setText("Kaikki tehtävät suoritettu.");
                }
            });

            Messages.sprintf("Kaikki tehtävät valmiita. Ikkuna jätetään auki raportointia varten.");
        } else {
            Messages.sprintf("Task finished, but " + activeTasksCount + " tasks are still running. Keeping window open.");
            unbind();
        }
    }

    /*** Manuaalinen sulkemismetodi, jota voidaan kutsua Controllerin sulje-painikkeesta.*/
    public static void closeStage() {
        Messages.sprintf("Closing loading stage window manually.");
        unbind();
        Platform.runLater(() -> {
            Stage stage = (Main.sceneManager != null) ? Main.sceneManager.getWindow_loadingprogress() : loadingStage;
            if (stage != null && stage.isShowing()) {
                stage.close();
            }
            loadingStage = null;
            loadingScene = null;
        });
    }
}