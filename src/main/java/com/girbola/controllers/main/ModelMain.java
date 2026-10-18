
package com.girbola.controllers.main;


import com.girbola.app.ApplicationContext;
import com.girbola.controllers.folderscanner.FolderScannerController;
import com.girbola.controllers.folderscanner.folderpicker.FolderSelectionService;
import com.girbola.controllers.main.selectedfolder.SelectedFolderScanner;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.drive.DriveInfo;
import com.girbola.messages.Messages;
import java.sql.Connection;
import java.util.Objects;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.ScheduledService;
import javafx.event.EventHandler;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class ModelMain {

    private final String ERROR = ModelMain.class.getSimpleName();

    private AnchorPane main_container;
    private BottomController bottomController;
    private FolderScannerController folderScannerController;
    private Buttons buttons;
    private Populate populate;
    private ScheduledService<Void> monitorExternalDriveConnectivity;
    private SelectedFolderScanner selectedFolders;
    private FolderSelectionService folderSelectionService;
    private StringProperty table_root_hbox_width = new SimpleStringProperty();
    private TablePositionHolder tablePositionHolder;
    private TabPane tabPaneMain;
    private Tables tables;
    private VBox main_vbox;
//    private WorkDirSQL workDirSQL; //TODO Move this to SQLHandler when it is ready
//    private SQLConfigurationHandler sqlConfigurationHandler;

    private ObservableList<DriveInfo> driveInfos = FXCollections.observableArrayList();
    private final ApplicationContext applicationContext;
    private final ModelMainPersistence persistence;
    private final ModelMainExitHandler exitHandler;

    public FolderSelectionService getFolderSelectionService() {
        return folderSelectionService;
    }

//    public void setFolderSelectionService(FolderSelectionService folderSelectionService) {
//        this.folderSelectionService = folderSelectionService;
//    }

    public ModelMain() {
        this(new LegacyMainApplicationContext());
    }

    public ModelMain(ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext, "applicationContext must not be null");
        Messages.sprintf("Model instantiated...");
        ModelMainConfigurationValidator.ValidationResult validation =
                ModelMainConfigurationValidator.validate(this.applicationContext.getConfiguration());
        if (validation == ModelMainConfigurationValidator.ValidationResult.MISSING_CONFIGURATION) {
            Messages.sprintfError("conf were null!!!!!!!!!!!!!!!!!!");
        } else if (validation == ModelMainConfigurationValidator.ValidationResult.MISSING_WORK_DIRECTORY) {
            Messages.sprintfError("workdir were empty");
        }
//        workDirSQL = new WorkDirSQL();
//        sqlConfigurationHandler = new SQLConfigurationHandler(conf);
        buttons = new Buttons(this);
        monitorExternalDriveConnectivity = new MonitorExternalDriveConnectivity(this);
        monitorExternalDriveConnectivity.setPeriod(Duration.seconds(15));
        populate = new Populate(this);
        selectedFolders = new SelectedFolderScanner();
        tablePositionHolder = new TablePositionHolder(this);
        tables = new Tables(this);
        folderSelectionService = new FolderSelectionService(this);
        tables.init();
        persistence = new ModelMainPersistence(applicationContext, tables);
        exitHandler = new ModelMainExitHandler(applicationContext, monitorExternalDriveConnectivity,
                this::saveAllTableContents);
    }

    public StringProperty getTable_root_hbox_width() {
        return table_root_hbox_width;
    }

    public SelectedFolderScanner getSelectedFolders() {
        return selectedFolders;
    }

    public Buttons buttons() {
        return this.buttons;
    }

    void setMainContainer(AnchorPane main_container) {
        this.main_container = main_container;
    }

    void setMainVBox(VBox main_vbox) {
        this.main_vbox = main_vbox;
    }

    public Tables tables() {
        return this.tables;
    }

    public Populate populate() {
        return this.populate;
    }

    public TabPane getTabPaneMain() { return tabPaneMain; }

    public void setTabPaneMain(TabPane tabPaneMain) { this.tabPaneMain = tabPaneMain;}

    public boolean saveAllTableContents() {
        return persistence.saveAllTableContents();
    }

    /**
     * Saves the content of the provided folder information list into the database based on the specified table type.
     * This method handles saving each folder's information, inserts associated file information into the database,
     * and commits the changes to the database.
     *
     * @param connectionConfiguration the database connection configuration to be used for saving the data
     * @param items                   the list of {@code FolderInfo} objects representing folder details to be saved
     * @param tableType               the type of table where the data should be saved
     * @return {@code true} if the data was saved and committed successfully, {@code false} otherwise
     */
    public synchronized boolean saveTableContent(Connection connectionConfiguration, ObservableList<FolderInfo> items, String tableType) {
        return persistence.saveTableContent(connectionConfiguration, items, tableType);
    }

    public void exitProgram_NOSAVE() {
        exitHandler.exitWithoutSaving();
    }

    public EventHandler<WindowEvent> exitProgram = event -> {
        Messages.sprintf("exitProgram");
        exitProgram();
        Messages.sprintf("exitProgram ended");
    };

    public EventHandler<WindowEvent> dontExitWindow = event -> event.consume();

    public void exitProgram() {
        exitHandler.exit();
    }

    public void load() {
        persistence.load(this);
    }

//    public SQLConfigurationHandler getSqlConfigurationHandler() {return this.sqlConfigurationHandler;}
//
//    public WorkDirSQL getWorkDirSQL() {return this.workDirSQL;}

    public ScheduledService<Void> getMonitorExternalDriveConnectivity() {
        return monitorExternalDriveConnectivity;
    }

    public void setBottomController(BottomController bottomController) {
        this.bottomController = bottomController;
    }

    public BottomController getBottomController() {
        return this.bottomController;
    }

    public ObservableList<DriveInfo> driveInfos() {
        return this.driveInfos;
    }

    public void setFolderScannerController(FolderScannerController folderScannerController) {
        this.folderScannerController = folderScannerController;
    }

    public FolderScannerController getFolderScannerController() {
        return this.folderScannerController;
    }
}
