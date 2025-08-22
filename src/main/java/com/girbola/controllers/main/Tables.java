package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.cell.*;
import com.girbola.controllers.main.tables.tabletype.TableType;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import com.girbola.sql.SqliteConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;



public class Tables {

    private final String ERROR = Tables.class.getSimpleName();

    private ModelMain model_Main;

    private SimpleDoubleProperty tablesParentWidth = new SimpleDoubleProperty(0);

    //	private HideButtons hideButtons;
    public ShowAndHideTables showAndHideTables;
    private TableStatistic tableStatistic;

    @Setter
    private Pane sortItRootPane;
    @Getter
    @Setter
    private Pane sortedRootPane;
    @Setter
    @Getter
    private Pane asItIsRootPane;

    private TableView<FolderInfo> sortIt_table;
    private TableView<FolderInfo> sorted_table;
    private TableView<FolderInfo> asitis_table;

    private AnchorPane tables_rootPane;

    @Getter
    @Setter
    private TableStatistic sortit_TableStatistic;

    @Getter
    @Setter
    private TableStatistic sorted_TableStatistic;

    @Getter
    @Setter
    private TableStatistic asitis_TableStatistic;

    boolean isSameTable = false;

    public AnchorPane getTables_rootPane() {
        return this.tables_rootPane;
    }

    public void setTables_rootPane(AnchorPane tables_rootPane) {
        this.tables_rootPane = tables_rootPane;
    }


//	private HBox tables_container;


    protected Tables(ModelMain aModel) {
        this.model_Main = aModel;

        sprintf("Tables instantiated...");
    }

    public void init() {

        sprintf("Tables hideButtons instantiating...");
        showAndHideTables = new ShowAndHideTables(this.model_Main);
        sprintf("Tables hideButtons instantiated...");
    }

    private FolderInfo findTableValues(TableView<FolderInfo> table, File f) {
        for (FolderInfo tv : table.getItems()) {
            if (tv.getFolderPath().equals(f.toString())) {
                return tv;
            }
        }
        sprintf("findTableValues is NULL");
        return null;
    }

    void setDrag(TableView<FolderInfo> table) {

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    if (event.getTarget() instanceof TableColumn) {
                        sprintf("setOnMouseClicked is tablerow");
                    } else {
                        sprintf("is not tablerow. it is:  " + event.getTarget().toString());
                    }
                }
            }
        });
        table.setOnDragDetected(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                if (event.getTarget() instanceof Rectangle) {
                    return;
                }
                if (event.getTarget().equals(event.getSource())) {
                    return;
                }
                sprintf("setOnDragDetected:  " + event.getTarget() + " source: " + event.getSource());

                ObservableList<FolderInfo> selected = table.getSelectionModel().getSelectedItems();
                List<File> selectedFiles = getSelectedFilesFromTable(selected);
                if (selectedFiles.isEmpty()) {
                    return;
                }
                Dragboard db = table.startDragAndDrop(TransferMode.MOVE);
                final ClipboardContent content = new ClipboardContent();
                content.putFiles(selectedFiles);
                db.setContent(content);

                event.consume();
            }

        });

        table.setOnDragDropped((DragEvent event) -> {
            isSameTable = false;
            sprintf("setOnDragDropped set on drag dropped: " + event.getGestureTarget() + " source gesture: "
                    + event.getGestureSource());

            if (event.getGestureSource() != event.getGestureTarget()) {
                @SuppressWarnings("unchecked")
                TableView<FolderInfo> source = (TableView<FolderInfo>) event.getGestureSource();
                @SuppressWarnings("unchecked")
                TableView<FolderInfo> target = (TableView<FolderInfo>) event.getGestureTarget();

                Messages.sprintf("target= " + target.getId() + " source= " + source.getId());
                // TableView<FolderInfo> tableTarget = (TableView<FolderInfo>)
                // event.getGestureTarget();
//				if (target == null) {
//					sprintf("tableTarget is null");
//					return;
//				}
                Dragboard db = event.getDragboard();
                List<File> folder = db.getFiles();
                if (folder.isEmpty()) {
                    // sprintf("db getFiles were empty!");
                    return;
                }
                for (File f : folder) {
                    Messages.sprintf("TEH file?: " + f);
                    FolderInfo tableValue = findTableValues(source, f);
                    if (tableValue != null) {
                        target.getItems().add(tableValue);
                    } else {
                        break;
                    }
                }
                TableUtils.calculateTableViewsStatistic(model_Main.tables());
            } else {
                isSameTable = true;
            }
        });

        table.setOnDragOver((DragEvent event) -> {
            /* show to the user that it is an actual gesture target */
            /*
             * if (event.getGestureSource() != target && event.getDragboard().hasString()) {
             * // allow for both copying and moving, whatever user chooses
             * event.acceptTransferModes(TransferMode.COPY_OR_MOVE); }
             */
            if (event.getGestureSource() != table && event.getDragboard().hasFiles()) {
                // sprintf("Event drag over has files: " +
                // event.getGestureSource() + " source:
                // " + event.getSource());
                event.acceptTransferModes(TransferMode.MOVE);
                isSameTable = false;
            } else {
                isSameTable = true;
            }
            event.consume();
        });
        table.setOnDragExited((DragEvent event) -> {
            sprintf("drag exited");
            event.consume();
        });

        table.setOnDragEntered((DragEvent event) -> {
            sprintf(" drag entered " + event.getGestureTarget());
            if (event.getGestureTarget() == null) {
                isSameTable = true;
            } else {
                isSameTable = false;
            }
        });
        table.setOnDragDone((DragEvent event) -> {
            sprintf("draggingg is done" + event.getGestureSource() + " target: " + event.getGestureTarget());

            if (!isSameTable) {
                @SuppressWarnings("unchecked")
                TableView<FolderInfo> tableView = (TableView<FolderInfo>) event.getGestureSource();
                ObservableList<FolderInfo> selectedToBeRemoved = tableView.getSelectionModel().getSelectedItems();
                tableView.getItems().removeAll(selectedToBeRemoved);
            }
            isSameTable = false;
            sortIt_table.getSelectionModel().clearSelection();
            sorted_table.getSelectionModel().clearSelection();
            asitis_table.getSelectionModel().clearSelection();

        });
    }

    public void setDeleteKeyPressed(TableView<FolderInfo> table) {
        table.setOnKeyPressed((KeyEvent event) -> {
            if (event.getCode() == (KeyCode.DELETE)) {
                ObservableList<FolderInfo> table_row_list = table.getSelectionModel().getSelectedItems();
                Dialog<ButtonType> dialog = Dialogs.createDialog_YesNoCancel(Main.sceneManager.getWindow(),
                        bundle.getString("removePermanently"));
                dialog.initStyle(StageStyle.UNDECORATED);
                Iterator<ButtonType> iterator = dialog.getDialogPane().getButtonTypes().iterator();
                while (iterator.hasNext()) {
                    ButtonType btn = iterator.next();
                    Messages.sprintf("BTNNTTNNT: " + btn.getText());
                }
                Optional<ButtonType> result = dialog.showAndWait();
                if (result.get().getButtonData().equals(ButtonBar.ButtonData.YES)) {

                    ArrayList<FolderInfo> listToRemove = new ArrayList<>();
                    Connection connection = SqliteConnection.connector(Main.conf.getAppDataPath(),
                            Main.conf.getConfiguration_db_fileName());

                    for (FolderInfo folderInfo : table_row_list) {
                        if (!folderInfo.getFolderPath()
                                .equals(System.getProperty("user.home") + File.separator + "Pictures")) {
                            listToRemove.add(folderInfo);
                        } else {
                            Messages.warningText(bundle.getString("folderIsUserHomeDirectory") + " Folder name is: "
                                    + folderInfo.getFolderPath());
                            listToRemove.add(folderInfo);
                        }
                    }
                    for (FolderInfo folderInfo : listToRemove) {
                        table.getItems().remove(folderInfo);
                    }
                    ConfigurationSQLHandler.insertIgnoredList(listToRemove);
                    listToRemove.clear();
                    try {
                        connection.close();
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.NO)) {
                    ArrayList<FolderInfo> listToRemove = new ArrayList<>();

                    for (FolderInfo folderInfo : table_row_list) {
                        // if
                        // (!folderInfo.getFolderPath().contains(System.getProperty("user.home")))
                        // {
                        //
                        // }
                        listToRemove.add(folderInfo);
                    }
                    table.getItems().removeAll(listToRemove);

                    listToRemove.clear();
                } else if (result.get().getButtonData().equals(ButtonBar.ButtonData.CANCEL_CLOSE)) {
                    Messages.sprintf("Cancelled removing files");
                } else {
                    Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), false);
                }

                // table.getSelectionModel().clearSelection();
            }
        });
    }

    public boolean checkIfExist(TableView<FolderInfo> tableTarget, String f) {
        for (FolderInfo folder : tableTarget.getItems()) {
            if (folder.getFolderPath().equals(f)) {
                // Messages.sprintf("folder listing: " + folder);
                return true;

            }
        }
        return false;
    }

    private List<File> getSelectedFilesFromTable(ObservableList<FolderInfo> list) {
        List<File> newList = new ArrayList<>();

        for (FolderInfo listing : list) {
            newList.add(new File(listing.getFolderPath()));
        }
        return newList;
    }

    public TableView<FolderInfo> getSortIt_table() {
        return this.sortIt_table;
    }

    public TableView<FolderInfo> getSorted_table() {
        return this.sorted_table;
    }

    public TableView<FolderInfo> getAsItIs_table() {
        return this.asitis_table;
    }

    public void setSortIt_table(TableView<FolderInfo> sortIt_table) {
        sprintf("setSortIt_table setted");
        if (sortIt_table == null) {
            Messages.sprintf("table was null!");
        }
        this.sortIt_table = sortIt_table;
        Messages.sprintf("table was: " + sortIt_table);
    }

    public void setSorted_table(TableView<FolderInfo> sorted_table) {
        if (sorted_table == null) {
            Messages.sprintf("table was null!");
        }
        this.sorted_table = sorted_table;
        
    }

    public void setAsItIs_table(TableView<FolderInfo> asitis_table) {
        if (asitis_table == null) {
            Messages.sprintf("table was null!");
        }
        this.asitis_table = asitis_table;
    }

//	void setTables_Container(HBox tables_container) {
//		this.tables_container = tables_container;
//		for (Node n : tables_container.getChildren()) {
//			if (n instanceof VBox) {
//
//				for (Node vbox : ((VBox) n).getChildren()) {
//					if (vbox instanceof TableView) {
//						sprintf("TableView found!: " + vbox);
//						HBox.setHgrow(vbox, Priority.ALWAYS);
//						HBox.setHgrow(vbox, Priority.ALWAYS);
//						HBox.setHgrow(vbox, Priority.ALWAYS);
//
//						VBox.setVgrow(vbox, Priority.ALWAYS);
//						VBox.setVgrow(vbox, Priority.ALWAYS);
//						VBox.setVgrow(vbox, Priority.ALWAYS);
//
//						HBox.setHgrow(n, Priority.ALWAYS);
//						HBox.setHgrow(n, Priority.ALWAYS);
//						HBox.setHgrow(n, Priority.ALWAYS);
//
//						VBox.setVgrow(n, Priority.ALWAYS);
//						VBox.setVgrow(n, Priority.ALWAYS);
//						VBox.setVgrow(n, Priority.ALWAYS);
//
//					}
//				}
//
//			}
//		}
//	}

//	HBox getTables_Container() {
//		return this.tables_container;
//	}

    public Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>> textFieldEditingCellFactory(
            FolderInfo folderInfo) {
        Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>> kala = new Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>>() {
            public TableCell<FolderInfo, String> call(TableColumn<FolderInfo, String> tableColumn) {
                return new EditingCell(model_Main, tableColumn);
            }
        };
        return kala;
    }

    public Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>> textFieldEditingCellFactory = new Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>>() {
        public TableCell<FolderInfo, String> call(TableColumn<FolderInfo, String> tableColumn) {
            return new EditingCell(model_Main, tableColumn);
        }
    };

    public Callback<TableColumn<FolderInfo, Boolean>, TableCell<FolderInfo, Boolean>> connected_cellFactory = new Callback<TableColumn<FolderInfo, Boolean>, TableCell<FolderInfo, Boolean>>() {
        @Override
        public TableCell<FolderInfo, Boolean> call(TableColumn<FolderInfo, Boolean> p) {
            return new TableCell_Connected(model_Main);
        }
    };

    public Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>> progressBar_cellFactory = new Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>>() {
        @Override
        public TableCell<FolderInfo, Integer> call(TableColumn<FolderInfo, Integer> p) {
            return new TableCell_ProgressBar();
        }
    };

    public Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>> dateFixer_cellFactory = new Callback<TableColumn<FolderInfo, String>, TableCell<FolderInfo, String>>() {
        @Override
        public TableCell<FolderInfo, String> call(TableColumn<FolderInfo, String> p) {
            return new TableCell_DateFixer(model_Main);
        }
    };
    public Callback<TableColumn<FolderInfo, Double>, TableCell<FolderInfo, Double>> dateDifference_Status_cellFactory = new Callback<TableColumn<FolderInfo, Double>, TableCell<FolderInfo, Double>>() {
        @Override
        public TableCell<FolderInfo, Double> call(TableColumn<FolderInfo, Double> p) {
            return new TableCell_DateDifference_Status(model_Main);
        }
    };
    public Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>> cell_Status_cellFactory = new Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>>() {
        @Override
        public TableCell<FolderInfo, Integer> call(TableColumn<FolderInfo, Integer> p) {
            return new TableCell_Status(model_Main);
        }
    };

    public Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>> copied_cellFactory = new Callback<TableColumn<FolderInfo, Integer>, TableCell<FolderInfo, Integer>>() {
        @Override
        public TableCell<FolderInfo, Integer> call(TableColumn<FolderInfo, Integer> p) {
            return new TableCell_Copied();
        }
    };

    public void refreshAllTables() {
        Platform.runLater(() -> {
            if (!getAsItIs_table().getItems().isEmpty()
                    || (TableColumn<?, ?>) getAsItIs_table().getColumns().get(0) != null) {
                ((TableColumn<?, ?>) getAsItIs_table().getColumns().get(0)).setVisible(false);
            }
            ((TableColumn<?, ?>) getAsItIs_table().getColumns().get(0)).setVisible(true);
        });
        Platform.runLater(() -> {
            if (!getSortIt_table().getItems().isEmpty()
                    || (TableColumn<?, ?>) getSortIt_table().getColumns().get(0) != null) {
                ((TableColumn<?, ?>) getSortIt_table().getColumns().get(0)).setVisible(false);
            }
            ((TableColumn<?, ?>) getSortIt_table().getColumns().get(0)).setVisible(true);
        });
        Platform.runLater(() -> {
            if (!getSorted_table().getItems().isEmpty()
                    || (TableColumn<?, ?>) getSorted_table().getColumns().get(0) != null) {
                ((TableColumn<?, ?>) getSorted_table().getColumns().get(0)).setVisible(false);
            }
            ((TableColumn<?, ?>) getSorted_table().getColumns().get(0)).setVisible(true);
        });
    }

    public void registerTableView_obs_listener() {
        getSorted_table().getItems().addListener(listener);
        getSortIt_table().getItems().addListener(listener);
        getAsItIs_table().getItems().addListener(listener);
    }

    ListChangeListener<FolderInfo> listener = new ListChangeListener<FolderInfo>() {

        @Override
        public void onChanged(Change<? extends FolderInfo> c) {
            Main.setChanged(true);
            sprintf("listener changed");
        }

    };

/*    public void setAsItIs_TableStatistic(TableStatistic asitis_TableStatistic) {
        this.asitis_TableStatistic = asitis_TableStatistic;
    }*/

    public TableView<FolderInfo> getTableByType(String tableType) {
        if (tableType.equals(TableType.SORTIT.getType())) {
            return getSortIt_table();
        } else if (tableType.equals(TableType.SORTED.getType())) {
            return getSorted_table();
        } else if (tableType.equals(TableType.ASITIS.getType())) {
            return getAsItIs_table();
        }
        return null;
    }

    public TableStatistic getTableStatisticByType(String tableType) {
        if (tableType.equals(TableType.SORTIT.getType())) {
            return getSortit_TableStatistic();
        } else if (tableType.equals(TableType.SORTED.getType())) {
            return getSorted_TableStatistic();
        } else if (tableType.equals(TableType.ASITIS.getType())) {
            return getAsitis_TableStatistic();
        }
        return null;
    }

    public double getTablesParentWidth() {
        return tablesParentWidth.get();
    }

    public SimpleDoubleProperty tablesParentWidthProperty() {
        return tablesParentWidth;
    }

    public void setTablesParentWidth(double tablesParentWidth) {
        this.tablesParentWidth.set(tablesParentWidth);
    }

}
