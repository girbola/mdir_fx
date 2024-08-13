package com.girbola.controllers.datefixer.utils;

import com.girbola.messages.Messages;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DateFixCommonUtils {

    public static ArrayList<LocalDateTime> createDateList_logic(int files, LocalDateTime start, LocalDateTime end) {
        Messages.sprintf("createDateList_logic");

        Duration duration = Duration.between(start, end);

        long seconds = ((long) (duration.getSeconds() / files));

        LocalDateTime runc = start;
        ArrayList<LocalDateTime> list = new ArrayList<>();
        list.add(runc);

        runc = runc.plusSeconds(seconds);
        files -= 2;
        for (int i = 0; i < files; i++) {
            runc = runc.plusSeconds(seconds);
            list.add(runc);
        }
        list.add(end);

        return list;
    }
}
