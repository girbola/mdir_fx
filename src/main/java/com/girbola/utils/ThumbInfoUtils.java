package com.girbola.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.thumbinfo.ThumbInfo;

import java.util.List;

public class ThumbInfoUtils {

    public static ThumbInfo findThumbInfo(FileInfo fileInfo, List<ThumbInfo> thumbInfoList, int id) {
        if(thumbInfoList == null) {
            Messages.sprintfError("thumbInfo_list is null");
            return new ThumbInfo(fileInfo.getOrgPath(), fileInfo.getFileInfo_id());
        }
        for (ThumbInfo thumbInfo : thumbInfoList) {
            if (thumbInfo.getId() == id) {
                Messages.sprintf("IMAGE FOUND: "+ fileInfo.getOrgPath());
                return thumbInfo;
            }
        }

        return new ThumbInfo(fileInfo.getOrgPath(), fileInfo.getFileInfo_id());

    }

}
