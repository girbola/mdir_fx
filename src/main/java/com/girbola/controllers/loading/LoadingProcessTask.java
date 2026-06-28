package com.girbola.controllers.loading;

import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;

public class LoadingProcessTask {

    private static final Logger LOGGER = Logger.getLogger(LoadingProcessTask.class.getName());
    private static final String ERROR_TAG = LoadingProcessTask.class.getSimpleName();

    private final ModelLoading modelLoading = new ModelLoading();
    private final Window owner;

    private double xOffset;
    private double yOffset;

    private Scene loadingScene;
    private Stage loadingStage;

    public LoadingProcessTask(Window owner) {
        this.owner = owner;
        // Run setup safely. If already on FX thread, runs immediately. If not, pushes to FX thread.
        if (Platform.isFxApplicationThread()) {
            Messages.sprintf("LoadingProcessTask: FX Application Thread");
            initGUI();
        } else {
            Messages.sprintf("LoadingProcessTask: Not FX Application Thread");
            Platform.runLater(this::initGUI);
        }
    }

    /**
     * Initializes the entire UI stack predictably.
     */
    private void initGUI() {
        try {
            // 1. Load FXML & Instantiate Parent
            FXMLLoader loader = loadFXML();
            if (loader == null || loadingScene == null) return;

            // 2. Initialize controller data
            LoadingProcessController lpc = loader.getController();
            if (lpc != null) {
                lpc.init(modelLoading);
            }

            // 3. Configure Stage
            setupLoadingStage();

            // 4. Register globally
            updateSceneSwitcher();

            showLoadStage();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Failed to load GUI", ex);
            Messages.errorSmth(ERROR_TAG, "Failed to load GUI", ex, Misc.getLineNumber(), true);
        }
    }

    private FXMLLoader loadFXML() throws IOException {
        URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
        if (fxmlLocation == null) {
            Messages.sprintfError("FXML resource not found");
            Platform.exit();
            return null;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        Parent parent = loader.load();

        // Scene creation should happen safely right here
        loadingScene = new Scene(parent);
        setupDragHandlers();
        applyStylesheet();

        return loader;
    }

    private void setupLoadingStage() {
        loadingStage = new Stage();
        loadingStage.setScene(loadingScene);

        // Set fixed dimensions tightly
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

    private void applyStylesheet() {
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

    private void setupDragHandlers() {
        loadingScene.setOnMousePressed(event -> {
            xOffset = loadingStage.getX() - event.getScreenX();
            yOffset = loadingStage.getY() - event.getScreenY();
        });

        loadingScene.setOnMouseDragged(event -> {
            loadingStage.setX(event.getScreenX() + xOffset);
            loadingStage.setY(Math.max(0, event.getScreenY() + yOffset));
        });
    }

    private void updateSceneSwitcher() {
        if (Main.sceneManager != null) {
            Main.sceneManager.setWindow_loadingprogress(loadingStage);
            Main.sceneManager.setScene_loading(loadingScene);
        } else {
            Messages.sprintfError("LoadingProcess_Task: Main.sceneManager was null!");
            Messages.warningText("LoadingProcess_Task: Main.sceneManager was null!");
        }
    }

    public void setTask(Task<?> currentTask) {
        if (modelLoading == null || currentTask == null) {
            Messages.sprintfError("LoadingProcess_Task: modelLoading or currentTask was null!");
            return;
        }
        // FIXED: Actually assign the task to your model!
        modelLoading.setTask(currentTask);
    }

    public void setProgress(double value, double max) {
        if (modelLoading.getProgressBar() != null) {
            Platform.runLater(() -> modelLoading.getProgressBar().setProgress(value / max));
        }
    }

    public void bind() {
        Task<?> task = modelLoading.getTask();
        if (task != null && modelLoading.getProgressBar() != null && modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> {
                modelLoading.getProgressBar().progressProperty().bind(task.progressProperty());
                modelLoading.getMessages_lbl().textProperty().bind(task.messageProperty());
            });
        } else {
            Messages.sprintf("Task, Progress Bar, or Label was null in bind()");
        }
    }

    public void updateTextArea(String message) {
        if (modelLoading.getMessages_txa() != null) {
            Platform.runLater(() -> {
                modelLoading.getMessages_txa().appendText(message + System.lineSeparator());
            });
        }
    }

    public void unbind() {
        Platform.runLater(() -> {
            if (modelLoading.getProgressBar() != null) {
                modelLoading.getProgressBar().progressProperty().unbind();
            }
            if (modelLoading.getMessages_lbl() != null) {
                modelLoading.getMessages_lbl().textProperty().unbind();
            }
        });
    }

    public void showLoadStage() {
        Stage stage = (Main.sceneManager != null) ? Main.sceneManager.getWindow_loadingprogress() : loadingStage;

        if (stage == null) {
            Messages.errorSmth(ERROR_TAG, "Loading scene has not been initialized.", null, Misc.getLineNumber(), true);
            return;
        }

        if (stage.isShowing()) {
            Messages.sprintf("Window is already showing!!");
            return;
        }

        if (modelLoading.getTask() == null && modelLoading.getProgressBar() != null) {
            Messages.sprintf("Task was null! Setting indeterminate progress.");
            Platform.runLater(() -> modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS));
        }

        Platform.runLater(stage::show);
    }

    public void closeStage() {
        Messages.sprintf("Closing loading stage window via fade-out animation");
        stopTask();
        unbind();

        Platform.runLater(() -> {
            Stage stage = (Main.sceneManager != null) ? Main.sceneManager.getWindow_loadingprogress() : loadingStage;
            Scene scene = (Main.sceneManager != null) ? Main.sceneManager.getScene_loading() : loadingScene;

            if (stage != null && scene != null && scene.getRoot() != null) {
                Timeline timeline = new Timeline();
                KeyFrame key = new KeyFrame(Duration.millis(300), // Reduced from 2000ms (2 seconds is too long a delay to block closing layouts!)
                        new KeyValue(scene.getRoot().opacityProperty(), 0));
                timeline.getKeyFrames().add(key);
                timeline.setOnFinished(event -> stage.close());
                timeline.play();
            } else if (stage != null) {
                stage.close();
            }
        });
    }

    private void stopTask() {
        Task<?> task = modelLoading.getTask();
        if (task != null && task.isRunning()) {
            task.cancel();
        }
    }

    public void setMessage(String message) {
        if (modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> modelLoading.getMessages_lbl().setText(message));
        }
    }

    public void bind(ReadOnlyStringProperty readOnlyStringProperty) {
        if (readOnlyStringProperty != null && modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> modelLoading.getMessages_lbl().textProperty().bind(readOnlyStringProperty));
        }
    }

    public void unbind(ReadOnlyStringProperty readOnlyStringProperty) {
        if (modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> modelLoading.getMessages_lbl().textProperty().unbind());
        }
    }

    public void bindTa(ReadOnlyStringProperty readOnlyStringProperty) {
        if (readOnlyStringProperty != null && modelLoading.getMessages_txa() != null) {
            Platform.runLater(() -> modelLoading.getMessages_txa().textProperty().bind(readOnlyStringProperty));
        }
    }

    public void unbindTa(ReadOnlyStringProperty readOnlyStringProperty) {
        if (modelLoading.getMessages_txa() != null) {
            Platform.runLater(() -> modelLoading.getMessages_txa().textProperty().unbind());
        }
    }
}