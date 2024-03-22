package com.girbola.controllers.datefixer.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class DateFixCommonUtils {
    public static ArrayList<LocalDateTime> createDateList_logic(int files, LocalDateTime start, LocalDateTime end) {
        System.out.println("createDateList_logic");

        Duration duration = Duration.between(start, end);

        System.out.println("LocalDateTime: " + duration.getSeconds() / files);
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

        for(LocalDateTime ld : list) {
            System.out.println("Localll: " + ld);
        }
        System.out.println("LOCAL SIZE: " + list.size());
        return list;
    }
}
