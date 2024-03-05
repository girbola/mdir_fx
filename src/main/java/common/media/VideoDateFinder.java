/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool (Pre-alpha)
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package common.media;

import common.utils.FileNameParseUtils;
import common.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.messages.Messages.sprintf;
import static common.utils.FileUtils.supportedVideo;

public class VideoDateFinder {

    public static Path hasTHMFile(Path path) {
        String orgFileName = FileNameParseUtils.parseFileExtentension(path);
        Path thmFile = Paths.get(orgFileName + ".THM");
        sprintf("trying to find thmFile. File name is: " + thmFile);
        if (Files.exists(thmFile)) {
            sprintf("THM FILE FOUND: " + thmFile);
            return thmFile;
        } else {
            return null;
        }
    }

    /**
     * getOriginalThumbFileForVideo will try to find original thumbnail file e.x
     * some camera brands records video file called IMG_555.MOV and it saves
     * thumbnailfile into same folder as IMG_555.THM
     *
     * @param file
     * @return thumb file path or null
     */
    public static Path getThumb_External_Image_FileForVideo(Path file) {

        String orgFileName = FileUtils.getExtension(file.toFile().toString());
        if (supportedVideo(file)) {
            sprintf("thmFile: " + file.getFileName());
            // int last =
            Path thmFile = Paths.get(orgFileName + ".THM");
            if (Files.exists(thmFile)) {
                sprintf("THM FILE FOUND: " + thmFile);
                return thmFile;
            } else {
                sprintf("THM FILE NOT FOUND: " + thmFile);
                return null;
            }

        }

        return null;
    }
}
