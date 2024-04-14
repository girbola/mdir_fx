package com.girbola.controllers.datefixer.tasks;

import com.girbola.Main;
import com.girbola.controllers.datefixer.Model_datefix;
import javafx.scene.Node;
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
            if (node_main instanceof VBox) {
                for (Node n : ((VBox) node_main).getChildren()) {
                    if (n instanceof HBox) {
                        for (Node hbc : ((HBox) n).getChildren()) {
                            if (hbc instanceof TextField) {
                                list.add(n);
                            }
                        }
                    }
                }
            }
        }
        return list;
    }
}
