package com.girbola.controllers.datefixer.tasks;

import com.girbola.Main;
import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.messages.Messages;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    protected static List<Node> create_listOfSelectedNodes(ModelDatefix model_datefix) {
        List<Node> list = new ArrayList<>();
        for (Node node_main : model_datefix.getSelectionModel().getSelectionList()) {
            if (node_main instanceof VBox vbox && vbox.getId().equals("imageFrame")) {
                Messages.sprintf("Inside imageFrame: " + node_main);
                list.add(node_main);
            }
        }
        return list;
    }
}