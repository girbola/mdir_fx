package com.girbola.controllers.datefixer.tasks;

import com.girbola.Main;
import com.girbola.controllers.datefixer.CssStylesEnum;
import com.girbola.controllers.datefixer.ModelDatefix;
import com.girbola.controllers.datefixer.utils.DateFixCommonUtils;
import com.girbola.controllers.datefixer.utils.DateFixGuiUtils;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.*;

import static com.girbola.messages.Messages.errorSmth;
import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;

public class MakeChanges extends Task<Integer> {

    private final String ERROR = MakeChanges.class.getSimpleName();

    private ArrayList<LocalDateTime> localDateTimeList;
    private LocalDateTime ldt_end;
    private LocalDateTime ldt_start;
    private ModelDatefix model_datefix;
    private int files;


    public MakeChanges(ModelDatefix model_datefix, LocalDateTime ldt_start, LocalDateTime ldt_end, int files) {
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
        for (Node node : list) {

                    Label fileDateLabel = DateFixGuiUtils.getFileDateLabel(node);
                    Messages.sprintf("Selected NODES: " + fileDateLabel.getId());

        }

        Collections.sort(dateList);

        list.sort((Node o1, Node o2) -> {
            int imageFrameImageNumber = DateFixGuiUtils.getImageFrameImageNumber((VBox) o1);
            int imageFrameImageNumber2 = DateFixGuiUtils.getImageFrameImageNumber((VBox) o2);
            if (imageFrameImageNumber < imageFrameImageNumber2) {
                return -1;
            } else if (imageFrameImageNumber > imageFrameImageNumber2) {
                return 1;
            } else {
                return 0;
            }
        });
        for (Node node : list) {
            Messages.sprintf("===SORTED Selected NODES: " + node.getId());
        }
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
            if (imageFrame instanceof VBox vbox && vbox.getId().equals("imageFrame")) {
                HBox bottomHBox = DateFixGuiUtils.getBottomHBox(vbox);
                Label fileDateLabel = DateFixGuiUtils.getFileDateLabel(vbox);
                Platform.runLater(() -> {
                    fileDateLabel.setText(it2.next());
                    bottomHBox.setStyle(CssStylesEnum.MODIFIED_STYLE.getStyle());
                });
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
