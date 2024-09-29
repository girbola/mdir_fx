package com.girbola.controllers.datefixer.tasks;

import com.girbola.Main;
import com.girbola.controllers.datefixer.Model_datefix;
import com.girbola.messages.Messages;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MakeChangesUtils {

    protected static List<String> getLocalDateTimeAsStringList(ArrayList<LocalDateTime> localDateTimeList) {
        List<String> collect = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = Main.simpleDates.getDtf_ymd_hms_minusDots_default();
        for (LocalDateTime localDateTime : localDateTimeList) {
            String format = dateTimeFormatter.format(localDateTime);
            collect.add(format);
        }
        return collect;
    }

    protected static List<Node> create_listOfSelectedNodes(Model_datefix model_datefix) {
        List<Node> list = new ArrayList<>();
        for (Node node_main : model_datefix.getSelectionModel().getSelectionList()) {
            if (node_main instanceof VBox && node_main.getId().equals("imageFrame")) {
                Messages.sprintf("Inside imageFrame: " + node_main);
                for (Node n : ((VBox) node_main).getChildren()) {
                    Messages.sprintf("Inside imageFrame: " + n);
                    if (n instanceof VBox && n.getId().equals("bottomContainer")) {
                        for (Node bottomContainer : ((VBox) n).getChildren()) {
                            if (bottomContainer instanceof HBox) {
                                for (Node hbox : ((HBox) bottomContainer).getChildren()) {
                                    if (hbox instanceof Label & hbox.getId().equals("fileDate")) {
                                        list.add(n);
                                    } else {
                                        Messages.sprintf("bottomContainer is not instanceof Label or bottomContainer.getId() is not fileDate " + bottomContainer);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}