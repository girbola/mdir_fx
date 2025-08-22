
package com.girbola.controllers.loading;

import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;


interface LoadingProcess_impl {

    public ProgressBar getProgressBar();

    public String getMessage();

    public void bind();

    public void closeWindow();

    public void setMessage(String message);

    public void setProgressBar(double progressBar);

    public void unbind();

    public void closeStage();

    public Stage getLoadStage();

    public void setStage(Stage stage);

    public Stage getStage();

    public void showLoadStage();

}
