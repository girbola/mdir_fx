
package common.media;

import common.utils.FileNameParseUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.girbola.messages.Messages.sprintf;
import static common.media.DateTaken.getMetadataDateTaken;
import static common.utils.FileUtils.supportedVideo;


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
