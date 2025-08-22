package com.girbola.controllers.operate;

import com.girbola.Main;
import com.girbola.dialogs.YesNoCancelDialogController;
import com.girbola.fileinfo.FileInfo;
import java.util.concurrent.Callable;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class Dialogue implements Callable<SimpleIntegerProperty> {
    private Window owner;
    private FileInfo fileInfo;
    private long copyedFileCurrentSize;
    private SimpleIntegerProperty answer;
    private SimpleStringProperty rememberAnswer;

    Dialogue(Window owner, FileInfo fileInfo, long copyedFileCurrentSize, SimpleIntegerProperty answer,
             SimpleStringProperty rememberAnswer) {
        this.owner = owner;
        this.fileInfo = fileInfo;
        this.copyedFileCurrentSize = copyedFileCurrentSize;
        this.answer = answer;
        this.rememberAnswer = rememberAnswer;
    }

    @Override
    public SimpleIntegerProperty call() throws Exception {
        FXMLLoader loader = null;
        Parent parent = null;
        YesNoCancelDialogController yesNoCancelDialogController = null;
        try {
            loader = new FXMLLoader(Main.class.getResource("dialogs/YesNoCancelDialog.fxml"), Main.bundle);
            parent = loader.load();

            yesNoCancelDialogController = (YesNoCancelDialogController) loader.getController();

            final Stage stage = new Stage();
            stage.setTitle(Main.bundle.getString("corruptedFile"));
            stage.initOwner(owner);
            stage.initStyle(StageStyle.UTILITY);
            stage.initModality(Modality.WINDOW_MODAL);
            yesNoCancelDialogController.init(stage, answer, fileInfo, rememberAnswer, "Corrupted file",
                    "Corrupted file found at " + fileInfo.getOrgPath() + " size should be: " + fileInfo.getSize()
                            + " but it is now " + copyedFileCurrentSize + "\n"
                            + Main.bundle.getString("doYouWantToKeepTheFile") + "",
                    Main.bundle.getString("yes"), Main.bundle.getString("no"), Main.bundle.getString("abort"));

            stage.setScene(new Scene(parent));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return answer;
    }
}