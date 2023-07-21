package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.sql.FileInfo_SQL;
import com.girbola.sql.SqliteConnection;
import javafx.scene.control.TableView;

import java.nio.file.Paths;
import java.sql.Connection;

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
							Main.conf.getMdir_db_fileName());
					boolean created = FileInfo_SQL.createFileInfoTable(connection);
					if (created) {
						FileInfo_SQL.insertFileInfoListToDatabase(connection, folderInfo.getFileInfoList(), false);
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
