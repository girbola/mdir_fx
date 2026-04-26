
package common.media;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.mov.QuickTimeDirectory;
import com.drew.metadata.mp4.media.Mp4VideoDirectory;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import common.utils.FileNameParseUtils;
import common.utils.FileUtils;

import java.nio.file.Path;
import java.util.Date;

import static com.girbola.messages.Messages.sprintf;
import static com.girbola.utils.FileInfoUtils.calculateFileSHA256;
import static com.girbola.utils.FileInfoUtils.getImageThumb_Offset_Length;


public class DateTaken {

    public static String getCameraModel(Metadata metaData) {

        Directory directory2 = metaData.getFirstDirectoryOfType(ExifIFD0Directory.class);
        try {
            return directory2.getString(0x110);
        } catch (Exception e) {
            return null;
        }

    }

    public static int getMetaDataOrientation(Metadata metaData) {

        Directory directory = metaData.getFirstDirectoryOfType(ExifIFD0Directory.class);
        if (directory != null) {
            try {
                return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
            } catch (MetadataException ex) {
                return 0;
            }
        }
        return 0;
    }


    public static String getCoordinates(Metadata metaData) {
        Directory directory = metaData.getFirstDirectoryOfType(GpsDirectory.class);
        if (directory != null) {
            GpsDirectory gpsDirectory = metaData.getFirstDirectoryOfType(GpsDirectory.class);

            for(com.drew.metadata.Tag tag : gpsDirectory.getTags()) {
                String description = tag.getDescription();
                System.out.println("Tag: " + tag.getTagName() + " - " + description);
            }
//            for (Directory dir : metaData.getDirectories()) {
//                System.out.println("Directory: " + dir.getName());
//                if (dir instanceof GpsDirectory) {
//                    System.out.println("Found GpsDirectory");
//                    for (com.drew.metadata.Tag tag : dir.getTags()) {
//                        String description = tag.getDescription();
//                        System.out.println("Tag: " + tag.getTagName() + " - " + description);
////            System.out.println("Tag: " + tag.getTagName() + " - " + tag.getDescription());
//                    }
//                }
//            }
            GeoLocation geoLocation = gpsDirectory.getGeoLocation();
            if (geoLocation == null) {
                return null;
            }
            return geoLocation.toString();
        }
        return null;
    }

//    public static long getCreationDate(Path path) {
//        if (FileUtils.supportedImage(path)) {
//            long date = getMetadataDateTaken(path);
//            if (date != 0) {
//                return date;
//            } else {
//                return 0;
//            }
//        } else if (FileUtils.supportedVideo(path)) {
//            String orgFileName = FileNameParseUtils.parseFileExtentension(path);
//            Path thmFile = Paths.get(path.getParent() + File.separator + orgFileName + ".THM");
//            sprintf("thmFile name is: " + thmFile);
//            if (Files.exists(thmFile)) {
//                sprintf("THM FILE FOUND: " + thmFile);
//                // ongelma;
//                long date = getMetadataDateTaken(thmFile);
//                sprintf("getDateThumbFileForVideo date: " + date);
//                return date;
//            } else {
//                sprintf("THM FILE NOT FOUND: " + thmFile);
//                return 0;
//            }
//        }
//        return 0;
//    }
//
//    public static Metadata getMetaData(Path path) {
//        try {
//            return ImageMetadataReader.readMetadata(path.toFile());
//        } catch (Exception e) {
//            return null;
//        }
//    }

//    public static long getMetadataDateTaken(Path path) {
//        Metadata metaData = null;
//        try {
//            metaData = ImageMetadataReader.readMetadata(path.toFile());
//        } catch (ImageProcessingException | IOException ex) {
//            return 0;
//        }
//        Iterable<Directory> directories = metaData.getDirectories();
//
//        for (Directory directory : directories) {
//            if (directory != null) {
//                return directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL).getTime();
//            }
//        }
//        return 0;
//    }

    public static boolean readMetadataInformation(Path path) {
        Metadata metaData = null;
        try {
            metaData = ImageMetadataReader.readMetadata(path.toFile());
        } catch (ImageProcessingException | IOException ex) {
            return false;
        }
        return true;
    }

    public static boolean defineFileInfo(FileInfo fileInfo) {
        Path path = Paths.get(fileInfo.getOrgPath());
        if (!Files.exists(path)) {
            Messages.sprintfError("File does not exists: " + path);
            return false;
        }
        Metadata metaData = null;
        try {
            metaData = ImageMetadataReader.readMetadata(path.toFile());
            if (fileInfo.getDate() == 0) {
                fileInfo.setDate(getMetaDataCreationDate(metaData, path));
            }
            if (fileInfo.getOrientation() == 0) {
                fileInfo.setOrientation(getMetaDataOrientation(metaData));
            }
            if (fileInfo.getCamera_model() == null || fileInfo.getCamera_model().isEmpty()) {
                fileInfo.setCamera_model(getCameraModel(metaData));
            }
            if (fileInfo.getThumb_offset() == 0) {
                getImageThumb_Offset_Length(metaData, fileInfo);
            }
            if (fileInfo.getSha256Checksum() == null || fileInfo.getSha256Checksum().isEmpty()) {
                fileInfo.setSha256Checksum(calculateFileSHA256(path));
            }

            return true;
        } catch (ImageProcessingException | IOException ex) {
            return false;
        }
    }

    public static long getMetaDataCreationDate(Metadata metaData, Path path) {

        Iterable<Directory> directories = metaData.getDirectories();

        if (FileUtils.supportedImage(path.toFile()) || FileUtils.supportedRaw(path.toFile())) {
            ExifSubIFDDirectory directory2 = metaData.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
// create a descriptor
            Date date = null;
            try {
                date = directory2.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    return date.getTime();
                }
            } catch (Exception ex) {

            }

            for (Directory directory : directories) {
                if (directory != null) {
                    date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (date != null) {
                        return date.getTime();
                    }
                }
            }
        } else if (FileUtils.supportedVideo(path)) {
            for (Directory directory : directories) {
                if (directory != null) {
                    Date videoDate_EXIF = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                    if (videoDate_EXIF != null) {
                        return videoDate_EXIF.getTime();
                    }
                    Date tag_creation_time_QUICKTIME = directory.getDate(QuickTimeDirectory.TAG_CREATION_TIME);
                    if (tag_creation_time_QUICKTIME != null) {
                        return tag_creation_time_QUICKTIME.getTime();
                    }
                    Date tag_creation_time_MP4 = directory.getDate(Mp4VideoDirectory.TAG_CREATION_TIME);
                    if (tag_creation_time_MP4 != null) {
                        return tag_creation_time_MP4.getTime();
                    }
                }
            }
        }
        return 0;
    }

    public static Metadata readMetaData(Path path) {
        try {
            return ImageMetadataReader.readMetadata(path.toFile());
        } catch (Exception e) {
            return null;
        }

    }
}
