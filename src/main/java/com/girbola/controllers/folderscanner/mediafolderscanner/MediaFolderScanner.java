package com.girbola.controllers.folderscanner.mediafolderscanner;
import common.utils.FileUtils;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MediaFolderScanner {

//    private static final Set<String> MEDIA_EXTENSIONS = Set.of(
//            "jpg", "jpeg", "png", "gif", "bmp", "webp",
//            "mp4", "mkv", "avi", "mov", "wmv", "flv"
//    );

    public static List<Path> findMediaFolders(Path rootFolder) throws IOException {
        List<Path> acceptedMediaFile = new ArrayList<>();

        Files.walkFileTree(rootFolder, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                // PASS 1: Quick-scan immediate files in this directory.
                // Break the absolute moment a media file is found.
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path path : stream) {
                        if (Files.isRegularFile(path) && FileUtils.getHasMedia(path.toFile())) {
                            acceptedMediaFile.add(dir);
                            break; // Stop looking at files in this folder immediately!
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Skipping restricted folder: " + dir + " (" + e.getMessage() + ")");
                    // Skip trying to visit this restricted folder's files or subfolders
                    return FileVisitResult.SKIP_SUBTREE;
                }

                // PASS 2: Tell walkFileTree to automatically continue into its subfolders
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Gracefully skip files/folders that throw permission errors during the walk
                return FileVisitResult.SKIP_SUBTREE;
            }
        });

        return acceptedMediaFile;
    }

//    private static boolean isMediaFile(Path file) {
//        String fileName = file.getFileName().toString().toLowerCase();
//        int dotIndex = fileName.lastIndexOf('.');
//        if (dotIndex == -1) {
//            return false;
//        }
//        String extension = fileName.substring(dotIndex + 1);
//        return hasMedia.contains(extension);
//    }
}