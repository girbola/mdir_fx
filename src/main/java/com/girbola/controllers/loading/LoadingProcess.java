package com.girbola.controllers.loading;



import com.girbola.MDir_Stylesheets_Constants;
import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import static com.girbola.Main.bundle;
import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

public class LoadingProcess {

    private static final String ERROR = LoadingProcessTask.class.getSimpleName();
    private static ModelLoading modelLoading = new ModelLoading();
    private static double xOffset;
    private static double yOffset;
    private static Parent parent = null;
    private static Window owner;

    private static Scene loadingScene;
    private static Stage loadingStage;

    public LoadingProcess(Window owner) {
        this.owner = owner;
        loadGUI();
    }

    /*
     * private Stage loadingStage; private Scene loadingScene;
     */
    public void setProgressBar(double progress) {
        modelLoading.getProgressBar().setProgress(progress);
    }

    public static void loadGUI() {
        Platform.runLater(() -> {
            try {
                // Load FXML
                FXMLLoader loader = loadFXML();
                if (loader == null) return;

                // Initialize controller
                LoadingProcessController lpc = (LoadingProcessController) loader.getController();
                lpc.init(modelLoading);

                // Configure stage
                setupLoadingStage(parent);

                // Configure scene
                setupLoadingScene();

                // Show stage and update scene switcher
                loadingStage.show();
                updateSceneSwitcher();

            } catch (Exception ex) {
                Logger.getLogger(LoadingProcessTask.class.getName())
                        .log(Level.SEVERE, "Failed to load GUI", ex);
                Messages.errorSmth(ERROR, "Failed to load GUI", ex, Misc.getLineNumber(), true);
            }
        });
    }

    private static FXMLLoader loadFXML() throws IOException {
        URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
        if (fxmlLocation == null) {
            Messages.sprintfError("FXML resource not found");
            Platform.exit();
            return null;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation, bundle);
        parent = loader.load();
        return loader;
    }

    private static void setupLoadingStage(Parent parent) {
        loadingScene = new Scene(parent);
        loadingStage = new Stage();

        // Set fixed dimensions
        loadingStage.setWidth(500);
        loadingStage.setHeight(400);
        loadingStage.setMinWidth(500);
        loadingStage.setMinHeight(400);
        loadingStage.setMaxWidth(500);
        loadingStage.setMaxHeight(400);

        if (owner != null) {
            loadingStage.initOwner(owner);
        }

        loadingStage.setTitle("loadingprocess_task: " + Main.conf.getWindowStartPosX());
        loadingStage.setAlwaysOnTop(true);
        Main.centerWindowDialog(loadingStage);
    }

    private static void setupLoadingScene() {
        // Add stylesheet
        String stylesheetPath = conf.getThemePath() + MDir_Stylesheets_Constants.LOADINGPROCESS.getType();
        loadingScene.getStylesheets().add(LoadingProcess.class.getResource(stylesheetPath).toExternalForm());

        // Store initial position
        xOffset = loadingStage.getX();
        yOffset = loadingStage.getY();

        // Add drag functionality
        setupDragHandlers();

        loadingStage.setScene(loadingScene);
    }

    private static void setupDragHandlers() {
        loadingScene.setOnMousePressed(event -> {
            xOffset = loadingStage.getX() - event.getScreenX();
            yOffset = loadingStage.getY() - event.getScreenY();
        });

        loadingScene.setOnMouseDragged(event -> {
            loadingStage.setX(event.getScreenX() + xOffset);
            loadingStage.setY(Math.max(0, event.getScreenY() + yOffset));
        });
    }

    private static void updateSceneSwitcher() {
        Main.sceneManager.setWindow_loadingprogress(loadingStage);
        Main.sceneManager.setScene_loading(loadingScene);
    }

    private void unbind() {
        if (modelLoading.getTask() != null) {
            modelLoading.getProgressBar().progressProperty().unbind();
            modelLoading.getMessages_lbl().textProperty().unbind();
        }
    }

    private void bind() {
        if (modelLoading.getTask() != null && modelLoading.getProgressBar() != null) {
            Platform.runLater(() -> {
                modelLoading.getProgressBar().progressProperty().bind(modelLoading.getTask().progressProperty());
                modelLoading.getMessages_lbl().textProperty().bind(modelLoading.getTask().messageProperty());
            });
        } else {
            sprintf("task or progress bar were null in BIND()");
        }
    }

    public void closeStage() {
        Messages.sprintf("closeStage is closing window");

        stopTask();
        unbind();

        Platform.runLater(() -> {
            if (Main.sceneManager.getScene_loading() != null && Main.sceneManager.getScene_loading().getRoot() != null && Main.sceneManager.getWindow_loadingprogress() != null) {

                Timeline timeline = new Timeline();
                KeyFrame key = new KeyFrame(Duration.millis(2000), new KeyValue(Main.sceneManager.getScene_loading().getRoot().opacityProperty(), 0));
                timeline.getKeyFrames().add(key);
                timeline.setOnFinished(event -> Main.sceneManager.getWindow_loadingprogress().close());
                timeline.play();
            } else {
                // Fallback if scene or root is null
                if (Main.sceneManager.getWindow_loadingprogress() != null) {
                    Main.sceneManager.getWindow_loadingprogress().close();
                }
            }
        });
    }

    private void stopTask() {
        if (modelLoading.getTask() != null) {
            if (modelLoading.getTask().isRunning()) {
                modelLoading.getTask().cancel();
            }
        }
    }

    public void showLoadStage() {
        if (Main.sceneManager.getWindow_loadingprogress().isShowing()) {
            Messages.sprintf("Window is already showing!!");
            return;
        }
        if (modelLoading.getTask() == null) {
            Messages.sprintf("Task were null!");
            Platform.runLater(() -> {
                modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });
        }

        Messages.sprintf("Showing stage!");

        Stage stage = Main.sceneManager.getWindow_loadingprogress();
        /*
         * if (owner != null) { stage.initOwner(owner); }
         */
        if (stage != null) {

            stage.show();

        } else {
            Messages.errorSmth(ERROR, "Loading scene haven't been initialisiz. It was null null!!!", null, Misc.getLineNumber(), true);
        }

    }
    public static void setMessage(String message) {
        // Initialize the loading GUI if not already showing
        if (loadingStage == null || !loadingStage.isShowing()) {
            Platform.runLater(() -> {


            loadGUI();
            });
        }

        Messages.sprintf("LoadingProcess_Task message= " + message);
        if (modelLoading.getMessages_lbl() != null) {
            Platform.runLater(() -> {
                modelLoading.getMessages_lbl().setText(message);
            });
        }
    }

}
