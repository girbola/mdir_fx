/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.girbola.Main;
import com.girbola.controllers.main.tables.FolderInfo;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.fileinfo.FileInfo;

import javafx.concurrent.Task;

import static com.girbola.Main.simpleDates;
import static com.girbola.controllers.main.tables.TableUtils.calculateDateDifferenceRatio;

/**
 *
 * @author Marko Lokka
 */
public class UpdateFolderInfoContent extends Task<Integer> {

    private FolderInfo folderInfo;

    public UpdateFolderInfoContent(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    int bad = 0;
    int good = 0;
    int image = 0;
    int raw = 0;
    int video = 0;
    int confirmed = 0;
    int ignored = 0;
    int mformat = 0;
    long size = 0;
    TreeMap<LocalDate, Integer> map = new TreeMap<>();

    List<Long> dateCounter_list = new ArrayList<>();

    @Override
    protected Integer call() throws Exception {
        for (FileInfo fi : folderInfo.getFileInfoList()) {
            if (Main.getProcessCancelled()) {
                break;
            }
            fi.getOrgPath();
//            fi.getDate();

            size += fi.getSize();

            if (fi.isBad()) {
                bad++;
            }
            if (fi.isConfirmed()) {
                confirmed++;
            }
            if (fi.isGood()) {
                good++;
            }
            if (fi.isIgnored()) {
                ignored++;
            }

            if (fi.isRaw()) {
                raw++;
            }
            if (fi.isImage()) {
                image++;
            }
            if (fi.isVideo()) {
                video++;
            }
            if (fi.getDate() != 0) {
                dateCounter_list.add(fi.getDate());
            } else {
                fi.getDate();
            }
            LocalDate localDate = null;
            try {
                localDate = LocalDate.of(
                        Integer.parseInt(simpleDates.getSdf_Year().format(fi.getDate())),
                        Integer.parseInt(simpleDates.getSdf_Month().format(fi.getDate())),
                        Integer.parseInt(simpleDates.getSdf_Day().format(fi.getDate())));

            } catch (Exception e) {
                Logger.getLogger(TableUtils.class.getName()).log(Level.SEVERE, null, e);
            }

            map.put(localDate, 0);
//            localDate = null;
        }

        folderInfo.setFolderFiles(image
                + raw + video);

        folderInfo.setBadFiles(bad);
//        folderInfo.setConfirmed(confirmed);

        folderInfo.setFolderRawFiles(raw);

        folderInfo.setFolderVideoFiles(video);

        folderInfo.setGoodFiles(good);

        folderInfo.setCopied(mformat);

        folderInfo.setFolderImageFiles(image);

        folderInfo.setFolderSize(size);
        long min = 0;
        long max = 0;

        if (!dateCounter_list.isEmpty()) {
            Collections.sort(dateCounter_list);
            try {
                min = Collections.min(dateCounter_list);
                max = Collections.max(dateCounter_list);

            } catch (Exception e) {
                Logger.getLogger(TableUtils.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        folderInfo.setMinDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(min));
        folderInfo.setMaxDate(simpleDates.getSdf_ymd_hms_minusDots_default().format(max));

        double dateDifferenceRatio = calculateDateDifferenceRatio(map);

        folderInfo.setDateDifferenceRatio(dateDifferenceRatio);
//        sprintf("Datedifference ratio completed");
//        folderInfo.setDateDifferenceRatio(0);

        dateCounter_list.clear();
        bad = 0;
        good = 0;
        image = 0;
        raw = 0;
        video = 0;
        confirmed = 0;
        return null;
    }

}
