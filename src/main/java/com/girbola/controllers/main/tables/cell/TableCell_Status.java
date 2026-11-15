package com.girbola.controllers.main.tables.cell;

import com.girbola.controllers.main.ModelMain;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.controllers.main.tables.tabletype.FolderInfoType;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

public class TableCell_Status extends TableCell<FolderInfo, Integer> {

    private ModelMain model_main;
    private int currentFilesTotal;
    private final Label statusBadge = new Label();

    private final String TABLECELLSTATUS_DONE = "tableCellStatus_DONE";
    private final String TABLECELLSTATUS_READY = "tableCellStatus_READY";
    private final String TABLECELLSTATUS_BAD = "tableCellStatus_BAD";
    private final String TABLECELLSTATUS_ADD = "tableCellStatus_ADD";
    private final String TABLECELLSTATUS_REMOVE = "tableCellStatus_REMOVE";

    public TableCell_Status(ModelMain model_main) {
        this.model_main = model_main;
        setAlignment(Pos.CENTER);
        statusBadge.setAlignment(Pos.CENTER);
    }

    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);

        statusBadge.getStyleClass().removeAll("tableCellStatusBadge", TABLECELLSTATUS_DONE, TABLECELLSTATUS_READY,
                TABLECELLSTATUS_BAD, TABLECELLSTATUS_ADD, TABLECELLSTATUS_REMOVE);
        if (empty || item == null) {
            setStyle("");
            setGraphic(null);
            setText(null);
        } else {
            FolderInfo folderInfo = getTableView().getItems().get(getIndex());
            if (folderInfo != null) {
                String statusText;
                String statusStyle;
                if (folderInfo.getFileInfoList().size() > 0
                        && folderInfo.getFileInfoList().size() == folderInfo.getCopied()
                        && folderInfo.getBadFiles() == 0) {
                    statusText = FolderInfoType.DONE.getType();
                    statusStyle = TABLECELLSTATUS_DONE;
                } else if (folderInfo.getFileInfoList().size() > 0 && folderInfo.getBadFiles() == 0) {
                    statusText = "OK";
                    statusStyle = TABLECELLSTATUS_READY;
                } else {
                    int procentage = (int) Math.floor(
                            ((double) folderInfo.getBadFiles() / (double) folderInfo.getFileInfoList().size()) * 100);
                    statusText = "" + procentage;
                    statusStyle = TABLECELLSTATUS_BAD;
                }
                if (folderInfo.getFolderFiles() > 0) {
                    if (currentFilesTotal > folderInfo.getFolderFiles()) {
                        currentFilesTotal = folderInfo.getFolderFiles();
                        statusText = FolderInfoType.ADD.getType();
                        statusStyle = TABLECELLSTATUS_ADD;
                    }
                }
                statusBadge.setText(statusText);
                statusBadge.getStyleClass().addAll("tableCellStatusBadge", statusStyle);
                setGraphic(statusBadge);
                setText(null);
            } else {
                setText(null);
                setGraphic(null);
            }
        }
    }
}
