package com.girbola.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.thumbinfo.ThumbInfo;

import java.util.List;

public class ThumbInfo_Utils {

    public static ThumbInfo findThumbInfo(FileInfo fileInfo, List<ThumbInfo> thumbInfo_list, int id) {
        for (ThumbInfo thumbInfo : thumbInfo_list) {
            if (thumbInfo.getId() == id) {
                return thumbInfo;
            }
        }

        return new ThumbInfo(fileInfo.getOrgPath(), fileInfo.getFileInfo_id());

    }

}
