package com.girbola.controllers.main;

import java.nio.file.Paths;
import java.sql.Connection;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.sql.SQL_Utils;
import com.girbola.sql.SqliteConnection;

import javafx.scene.control.TableView;

public class SaveTableContents {
	private Tables tables;
	private Connection connection;

	public SaveTableContents(Tables tables) {
		this.tables = tables;
	}

	
	public void save_All_TableViews_FolderInfos_toSQL() throws Exception {
		saveToSQL_DB(tables.getSortIt_table());
		saveToSQL_DB(tables.getSorted_table());
		saveToSQL_DB(tables.getAsItIs_table());
	}


	private void saveToSQL_DB(TableView<FolderInfo> table) {
		if (!table.getItems().isEmpty()) {

			for (FolderInfo folderInfo : table.getItems()) {
				if (!folderInfo.getFileInfoList().isEmpty()) {
					connection = SqliteConnection.connector(Paths.get(folderInfo.getFolderPath()),
							Main.conf.getFileInfo_db_fileName());
					boolean created = SQL_Utils.createFileInfoTable(connection);
					if (created) {
						SQL_Utils.insertFileInfoListToDatabase(connection, folderInfo.getFileInfoList(), false);
					}
				}

			}
			try {
				connection.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
