package com.girbola.controllers.viewimages;

import com.girbola.controllers.datefixer.DateFixConstants;
import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import javafx.scene.Node;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ViewImages {
    private FileInfo fileInfoSelected;
    private ModelDatefix modelDatefix;

    public ViewImages(FileInfo fileInfoSelected, ModelDatefix modelDatefix) {
        this.fileInfoSelected = fileInfoSelected;
        this.modelDatefix = modelDatefix;
    }

    public void init() {
        Messages.sprintf("fileInfoSelected: " + fileInfoSelected.getOrgPath());
        TilePane tilePane = modelDatefix.getTilePane();
        Messages.sprintf("tilePane: " + tilePane.getId());
        if(tilePane != null && tilePane.getId().equals(DateFixConstants.DFTILEPANE.getType())) {
            for(Node node : tilePane.getChildren()) {
                if(node instanceof VBox && node.getId().equals(DateFixConstants.IMAGEFRAME.getType())) {
                    FileInfo fileInfo = (FileInfo) node.getUserData();
                    Messages.sprintf("node: " + fileInfo.getOrgPath());
                    if(fileInfoSelected.equals(fileInfo)) {
                        Messages.sprintf("Found selected image: " + fileInfo.getOrgPath());
                    }
                }
            }
        }
    }
}
