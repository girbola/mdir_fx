package com.girbola.controllers.datefixer.tasks;

import com.girbola.controllers.datefixer.CssStylesController;
import com.girbola.controllers.datefixer.DateTimeAdjusterController;
import com.girbola.controllers.datefixer.Model_datefix;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

public class MakeChanges extends Task<Integer> {

    private final String ERROR = MakeChanges.class.getSimpleName();

    private ArrayList<LocalDateTime> localDateTimeList;
    private Model_datefix model_datefix;

    public MakeChanges(Model_datefix model_datefix, ArrayList<LocalDateTime> localDateTimeList) {
        this.model_datefix = model_datefix;
        this.localDateTimeList = localDateTimeList;
    }

    @Override
    protected Integer call() throws Exception {

        List<String> dateList = MakeChangesUtils.getLocalDateTimeAsStringList(localDateTimeList);

        List<Node> list = MakeChangesUtils.create_listOfSelectedNodes(model_datefix);

        if (list.isEmpty()) {
            errorSmth(ERROR, "List were empty", null, getLineNumber(), true);
        }

        Collections.sort(dateList);

        list.sort((Node o1, Node o2) -> {
            String value1 = o1.getId().replace("fileDate: ", "");
            if (value1.length() <= 1) {
                value1 = "0" + value1;
                sprintf("Zero added: " + value1);
            }
            String value2 = o2.getId().replace("fileDate: ", "");
            if (value2.length() <= 1) {
                value2 = "0" + value2;
                sprintf("Zero added: " + value2);
            }
            return value1.compareTo(value2);
        });
        for (String dl : dateList) {
            sprintf("DLLIST: " + dl);
        }
        Iterator<Node> it = list.iterator();
        Iterator<String> it2 = dateList.iterator();
        if (list.size() != dateList.size()) {
            sprintf("list size is: " + list.size() + " dateList size is: " + dateList.size());
            errorSmth(ERROR, "Lists were different", null, getLineNumber(), true);
        }
        while (it.hasNext() && it2.hasNext()) {
            Node node = it.next();
            if (node instanceof HBox) {
                for (Node nodeHBox : ((HBox) node).getChildren()) {
                    if (nodeHBox instanceof TextField) {
                        try {
                            TextField tf = (TextField) nodeHBox;
                            tf.setText(it2.next());
                            tf.setStyle(CssStylesController.getModified_style());
                        } catch (Exception ex) {
                            errorSmth(ERROR, "Cannot make textfield changes. " + (it == null ? true : false), ex, Misc.getLineNumber(), true);
                        }
                    }
                }
            }
        }
        return null;
    }
}
