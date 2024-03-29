package com.girbola.controllers.main.tables.cell;

import com.girbola.Main;
import com.girbola.controllers.datefixer.GUI_Methods;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;

public class TableCell_Connected extends TableCell<FolderInfo,
		Boolean> {

	private Button tryToReconnect = new Button();
	private StackPane stackPane = new StackPane();
	private Model_main model_main;

	public TableCell_Connected(Model_main model_main) {
		this.model_main = model_main;
		ImageView reload_iv = new ImageView(GUI_Methods.loadImage("reload.png", 15));
		tryToReconnect.setGraphic(reload_iv);
		tryToReconnect.getStyleClass().add("transparent_btn");

	}

	@Override
	protected void updateItem(Boolean item, boolean empty) {
		super.updateItem(item, empty);
		if (empty) {
			setGraphic(null);
			setText(null);
		} else {
			setGraphic(stackPane);
			setText(null);
			FolderInfo folderInfo = (FolderInfo) getTableView().getItems().get(getIndex());
			if (!folderInfo.isConnected()) {
				if (!stackPane.getChildren().contains(tryToReconnect)) {
					setBad(folderInfo);
				}
			} else {
				setGood();
				setGraphic(null);
				setText(null);
			}
			setGraphic(stackPane);
			setText(null);
		}

	}

	private void setBad(FolderInfo folderInfo) {
		stackPane.getChildren().add(tryToReconnect);
		stackPane.setStyle("-fx-background-color: derive(red, 50%);");
		tryToReconnect.setOnAction(event -> {
			if (Files.exists(Paths.get(folderInfo.getFolderPath()))) {
				setGood();
				Connection connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()), Main.conf.getMdir_db_fileName());
				List<FileInfo> list = FileInfo_SQL.loadFileInfoDatabase(connection);
				folderInfo.getFileInfoList().addAll(list);
				TableUtils.updateFolderInfo(folderInfo);
				TableUtils.refreshTableContent(model_main.tables().getSorted_table());
				TableUtils.refreshTableContent(model_main.tables().getSortIt_table());
				TableUtils.refreshTableContent(model_main.tables().getAsItIs_table());

				SQL_Utils.closeConnection(connection);

			} else {
				Messages.sprintf("Still not exists");
				//				setGood();
			}
		});
	}

	private void setGood() {
		stackPane.setStyle("-fx-background-color: derive(blue, 20%);");
		stackPane.getChildren().remove(tryToReconnect);

	}

}
