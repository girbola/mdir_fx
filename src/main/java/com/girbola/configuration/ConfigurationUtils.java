package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.WriteTablesFolderInfoToConfigurationDatabase;
import com.girbola.fileinfo.SavedFoldersIntoConfigurationTable;
import com.girbola.messages.Messages;
import javafx.concurrent.Task;

public class ConfigurationUtils {

//    final private static String ERROR = ConfigurationUtils.class.getSimpleName();

    public static void saveTablesToConfigurationDatabase(ModelMain modelMain) {
        SavedFoldersIntoConfigurationTable saveFileInfos =
                new SavedFoldersIntoConfigurationTable(modelMain, Main.sceneManager.getWindow(), null, true);
        saveFileInfos.readTables();

        Task<Integer> writeTablesFolderInfoToConfigurationDatabase =
                new WriteTablesFolderInfoToConfigurationDatabase(modelMain, Main.sceneManager.getWindow(), null, true);

        writeTablesFolderInfoToConfigurationDatabase.setOnSucceeded(event ->
                Messages.sprintfError("saveTablesToDatabases succeeded"));
        writeTablesFolderInfoToConfigurationDatabase.setOnFailed(event ->
                Messages.sprintfError("saveTablesToDatabases failed"));
        writeTablesFolderInfoToConfigurationDatabase.setOnCancelled(event ->
                Messages.sprintfError("saveTablesToDatabases cancelled"));

        Thread thread = new Thread(
                writeTablesFolderInfoToConfigurationDatabase,
                "Saving data MenuBarController Thread"
        );
        thread.setDaemon(true);
        thread.start();
    }

}
