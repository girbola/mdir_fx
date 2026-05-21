package com.girbola.controllers.main.folderinfoscan;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import lombok.Getter;

@Getter
public class FolderViewModel {

    private final ObservableList<FolderInfoViewState> folderInfoViewStates = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public void submit(FolderInfoViewState folderInfoViewState) {

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

// Update UI state safely
                javafx.application.Platform.runLater(() -> folderInfoViewState.setState(FolderInfoScannerActionStateEnum.PROCESSING));

                try {
// Simulate work (e.g., file processing)
                    Thread.sleep(2000);

                    javafx.application.Platform.runLater(() -> folderInfoViewState.setState(FolderInfoScannerActionStateEnum.DONE));

                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> folderInfoViewState.setState(FolderInfoScannerActionStateEnum.ERROR));
                }

                return null;
            }
        };

        executor.submit(task);
    }
}
