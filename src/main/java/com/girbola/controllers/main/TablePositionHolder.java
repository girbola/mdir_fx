/*
 @(#)Copyright:  Copyright (c) 2012-2020 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.girbola.controllers.main.tables.tabletype.TableType;

class TablePositionHolder implements Serializable {

    private Model_main model_main;

    private Map<Path, TableType> tablePositionHolder_map = new HashMap<>();

    public TablePositionHolder(Model_main aModel_main) {
        this.model_main = aModel_main;
    }

    public TableType getTableType(Path path) {
        for (Iterator<Entry<Path, TableType>> it = tablePositionHolder_map.entrySet().iterator(); it.hasNext();) {
            Entry<Path, TableType> entry = it.next();
            if (entry.getKey().equals(path)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void addToTablePosition(Path path, TableType type) {
        tablePositionHolder_map.put(path, type);
    }

    public void removePossiblePosition(Path path) {
        tablePositionHolder_map.entrySet().stream().filter((entry) -> (entry.getKey().equals(path))).forEachOrdered((_item) -> {
            tablePositionHolder_map.remove(path);
        });
    }

    public void removeFromTablePosition(Path path) {
        tablePositionHolder_map.remove(path);
    }
//
//    public void loadTablePositions(Path path) {
//        
//        for (FolderInfo folderInfo : model_main.getTables().getSorted_table().getItems()) {
//            tablePositionHolder_map.put(Paths.get(folderInfo.getFolderPath()), TableType.SORTED);
//        }
//        for (FolderInfo folderInfo : model_main.getTables().getSortIt_table().getItems()) {
//            tablePositionHolder_map.put(Paths.get(folderInfo.getFolderPath()), TableType.SORTIT);
//        }
//
//        for (FolderInfo folderInfo : model_main.getTables().getAsItIs_table().getItems()) {
//            tablePositionHolder_map.put(Paths.get(folderInfo.getFolderPath()), TableType.ASITIS);
//        }
//    }

}
