
package com.girbola.controllers.misc;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.sql.ConfigurationSQLHandler;
import com.girbola.messages.Messages;
import javafx.application.Platform;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.misc.Misc.getLineNumber;


public class Misc_GUI {

    public static void fastExit() {
        sprintf("exitProgram " + getLineNumber());
        ConfigurationSQLHandler.close();
        sprintf("DELETING RUNNING DAT FILE DEMOOOOOOOOOOOOOOOOOOO FIX THIS BEFORE RELEASE" + getLineNumber());
        ConcurrencyUtils.stopAllExecThreadNow();
        Platform.exit();
    }

    public static void autoResizeColumns(TableView<?> table, double extraSpace) {
        //Set the right policy
        //table.setColumnResizePolicy( TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach((column) -> {
            //Minimal width = columnheader
            Text t = new Text(column.getText());
            double fontSize = t.getFont().getSize();
            //Messages.sprintf("FOLDER::: "+ t + " *********t.getLayoutBounds().getWidth(): " + fontSize);
            double max = Math.max(t.getLayoutBounds().getWidth(), fontSize * column.getText().length() * 0.6);
            for (int i = 0; i < table.getItems().size(); i++) {
                //cell must not be empty
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    //remember new max-width
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            //set the new max-width with some extra space
            column.setPrefWidth(max);
        });
    }

}
