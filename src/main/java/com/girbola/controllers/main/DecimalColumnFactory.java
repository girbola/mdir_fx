
package com.girbola.controllers.main;

import com.girbola.messages.Messages;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.DecimalFormat;


public class DecimalColumnFactory<S, T extends Number> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    private DecimalFormat format;

    public DecimalColumnFactory(DecimalFormat format) {
        super();
        this.format = format;
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<S, T>() {

            @Override
            protected void updateItem(T item, boolean empty) {
                if (!empty && item != null) {

                    setText(""+((item.longValue())));
                } else {
                    setText("");
                }
            }
        };
    }

    public static long convertDoubleToMB(long bytes) {
        int length = (int) Math.log10(bytes) + 1;
        Messages.sprintf("convertDoubleToMB Lenght: " + length);
        if (length >= 10) {
            return bytes /(long)  Math.pow(10,(length-4));
        } else if (length > 9) {
            return bytes / (10 * length);
        } else if (length > 8) {
            return bytes / (10 * length);
        } else if (length > 7) {
            return bytes / (10 * length);
        }
        long exp = (int) (Math.log(bytes) / Math.log(1024));
        return exp;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
