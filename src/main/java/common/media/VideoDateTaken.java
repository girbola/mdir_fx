/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.media;

import common.utils.FileNameParseUtils;
import static common.utils.FileUtils.supportedVideo;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.messages.Messages.sprintf;
import static common.media.DateTaken.getMetadataDateTaken;

/**
 *
 * @author Marko Lokka
 */
public class VideoDateTaken {

    /**
     * getDateThumbFileForVideo will try to find original thumbnail file example
     * some camera brands records video file called IMG_5555.MOV and it saves
     * thumbnailfile into same folder as IMG_5555.THM where EXIF info exists
     *
     * @param path
     * @return thumb file takendate or null
     */
    public static long getDateThumbFileForVideo(Path path) {

        if (supportedVideo(path)) {
            String orgFileName = FileNameParseUtils.parseFileExtentension(path);
            Path thmFile = Paths.get(path.getParent() + File.separator + orgFileName + ".THM");
            sprintf("thmFile name is: " + thmFile);
            if (Files.exists(thmFile)) {
                sprintf("THM FILE FOUND: " + thmFile);
//                ongelma;
                long date = getMetadataDateTaken(thmFile);
                sprintf("getDateThumbFileForVideo date: " + date);
                return date;
            } else {
                sprintf("THM FILE NOT FOUND: " + thmFile);
                return 0;
            }

        }

        return 0;
    }
}
