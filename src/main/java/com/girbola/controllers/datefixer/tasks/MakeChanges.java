package com.girbola.controllers.datefixer.tasks;

import com.girbola.Main;
import com.girbola.controllers.datefixer.CssStylesEnum;
import com.girbola.controllers.datefixer.Model_datefix;
import com.girbola.controllers.datefixer.utils.DateFixCommonUtils;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
    private LocalDateTime ldt_end;
    private LocalDateTime ldt_start;
    private Model_datefix model_datefix;
    private int files;


    public MakeChanges(Model_datefix model_datefix, LocalDateTime ldt_start, LocalDateTime ldt_end, int files) {
        this.model_datefix = model_datefix;
        this.ldt_start = ldt_start;
        this.ldt_end = ldt_end;
        this.files = files;
        Main.setChanged(true);
    }

    @Override
    protected Integer call() throws Exception {

        if (files < 2) {
            localDateTimeList = new ArrayList<>();
            localDateTimeList.add(ldt_start);
        } else {
            localDateTimeList = DateFixCommonUtils.createDateList_logic(files, ldt_start, ldt_end);
        }

        if (localDateTimeList.isEmpty()) {
            errorSmth(ERROR, "List were empty", null, getLineNumber(), true);
            failed();
            Main.setProcessCancelled(true);
            return null;
        }

        List<String> dateList = MakeChangesUtils.getLocalDateTimeAsStringList(localDateTimeList);
        for (String datee : dateList) {
            Messages.sprintf("DATEEEE OF LIST: " + datee);
        }

        List<Node> list = MakeChangesUtils.create_listOfSelectedNodes(model_datefix);

        if (list.isEmpty()) {
            errorSmth(ERROR, "List were empty", null, getLineNumber(), true);
        }

        Collections.sort(dateList);

        list.sort((Node o1, Node o2) -> {
            String value1 = o1.getId().replace("fileDate: ", "");
            if (value1.length() <= 1) {
                value1 = "0" + value1;
            }
            String value2 = o2.getId().replace("fileDate: ", "");
            if (value2.length() <= 1) {
                value2 = "0" + value2;
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
            Node imageFrame = it.next();
            if (imageFrame instanceof VBox) {
                for (Node nodeVBox : ((VBox) imageFrame).getChildren()) {
                    if (nodeVBox instanceof HBox && nodeVBox.getId().equals("bottom")) {
                        for (Node bottom : ((HBox) nodeVBox).getChildren()) {
                            if (bottom instanceof Label && bottom.getId().equals("fileDate")) {
                                Label tf = (Label) bottom;
                                tf.setText(it2.next());
                                nodeVBox.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
    }

    @Override
    protected void failed() {
        super.failed();
    }

}
