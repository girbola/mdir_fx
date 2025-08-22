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
import javafx.concurrent.Task;
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

public class LoadingProcessTask {

    private final String ERROR = LoadingProcessTask.class.getSimpleName();
    private ModelLoading modelLoading = new ModelLoading();
    private double xOffset;
    private double yOffset;
    private Parent parent = null;
    private Window owner;

    private Scene loadingScene;
    private Stage loadingStage;

    public LoadingProcessTask(Window owner) {
        this.owner = owner;
        loadGUI();
    }

    /*
     * private Stage loadingStage; private Scene loadingScene;
     */
    public void setProgressBar(double progress) {
        modelLoading.getProgressBar().setProgress(progress);
    }

    public void setTask(Task<?> current_Task) {
        if (modelLoading == null) {
            Messages.sprintfError("ModelLoading is not initialized");
            return;
        }

        if (current_Task == null) {
            Messages.sprintf("LoadingProcess_Task Task were set to null!!");
            return;
        }

//        Platform.runLater(() -> {
//            try {
//                modelLoading.getProgressBar().setProgress(ProgressBar.INDETERMINATE_PROGRESS);
//                if (!Main.getProcessCancelled()) {
//                    if (Main.sceneManager.getWindow_loadingprogress() != null
//                            && Main.sceneManager.getWindow_loadingprogress().isShowing()) {
//                        modelLoading.setTask(current_Task);
//                        bind();
//                    } else {
//                        modelLoading.setTask(current_Task);
//                        bind();
//                        loadGUI();
//                    }
//                } else {
//                    closeStage();
//                }
//            } catch (Exception ex) {
//                Messages.sprintfError("Error setting task: " + ex.getMessage());
//
//                Logger.getLogger(LoadingProcessTask.class.getName()).log(Level.SEVERE, null, ex);
//                Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
//            }
//            if (Main.getProcessCancelled()) {}
//        });
    }
    public void loadGUI() {
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

    private FXMLLoader loadFXML() throws IOException {
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

    private void setupLoadingStage(Parent parent) {
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

    private void setupLoadingScene() {
        // Add stylesheet
        String stylesheetPath = conf.getThemePath() + MDir_Stylesheets_Constants.LOADINGPROCESS.getType();
        loadingScene.getStylesheets().add(getClass().getResource(stylesheetPath).toExternalForm());

        // Store initial position
        xOffset = loadingStage.getX();
        yOffset = loadingStage.getY();

        // Add drag functionality
        setupDragHandlers();

        loadingStage.setScene(loadingScene);
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
        Main.sceneManager.setWindow_loadingprogress(loadingStage);
        Main.sceneManager.setScene_loading(loadingScene);
    }
    public void loadGUI_() {
        Platform.runLater(() -> {
            FXMLLoader loader = null;
            URL fxmlLocation = Main.class.getResource("/com/girbola/fxml/loading/LoadingProcess.fxml");
            if (fxmlLocation == null) {
                Messages.sprintfError("FXML resource not found: " + fxmlLocation);
                Platform.exit();
                return;
            }

            try {
                loader = new FXMLLoader(fxmlLocation, bundle);
                parent = loader.load();
            } catch (IOException ex) {
                Logger.getLogger(LoadingProcessTask.class.getName()).log(Level.SEVERE, null, ex);
                Messages.errorSmth(ERROR, "Failed to load FXML", ex, Misc.getLineNumber(), true);
                return;
            }

            LoadingProcessController lpc = (LoadingProcessController) loader.getController();
            lpc.init(modelLoading);
            loadingScene = new Scene(parent);
            loadingStage = new Stage();
            loadingStage.setWidth(500);
            loadingStage.setMinWidth(500);
            loadingStage.setMaxWidth(500);
            loadingStage.setHeight(400);
            loadingStage.setMinHeight(400);
            loadingStage.setMaxHeight(400);

            if (owner != null) {
                loadingStage.initOwner(owner);
            }
//			loadingStage.initStyle(StageStyle.UNDECORATED);
            Messages.sprintf("Owner is: " + loadingStage.getOwner());
//		loadingStage.setX(Main.conf.getWindowStartPosX());
            loadingStage.setTitle("loadingprocess_task: " + Main.conf.getWindowStartPosX());
            loadingScene.getStylesheets().add(getClass().getResource(conf.getThemePath() + MDir_Stylesheets_Constants.LOADINGPROCESS.getType()).toExternalForm());

            xOffset = loadingStage.getX();
            yOffset = loadingStage.getY();

            Main.centerWindowDialog(loadingStage);
            loadingScene.setOnMousePressed(event -> {
                xOffset = (loadingStage.getX() - event.getScreenX());
                yOffset = (loadingStage.getY() - event.getScreenY());
                sprintf("yOffset: " + yOffset);
            });

            loadingScene.setOnMouseDragged(event -> {
                loadingStage.setX(event.getScreenX() + xOffset);
                if (event.getScreenY() <= 0) {
                    loadingStage.setY(0);
                } else {
                    loadingStage.setY(event.getScreenY() + yOffset);
                }

                sprintf("event.getScreenY(); = " + event.getScreenY());
            });

            loadingStage.setScene(loadingScene);
            loadingStage.setAlwaysOnTop(true);

            loadingStage.show();
            Main.sceneManager.setWindow_loadingprogress(loadingStage);
            Main.sceneManager.setScene_loading(loadingScene);
        });

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

    public void setMessage(String message) {
        Messages.sprintf("LoadingProcess_Task message= " + message);
        if (Main.sceneManager.getWindow_loadingprogress() != null) {
            if (Main.sceneManager.getWindow_loadingprogress().isShowing()) {
                Platform.runLater(() -> {
                    modelLoading.getMessages_lbl().setText(message);
                });
            }
        }
    }

}
