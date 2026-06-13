package com.girbola.utils.folderscanner;

import com.girbola.filelisting.ValidatePathUtils;
import common.utils.FileUtils;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FolderScanner {

    public static List<Path> scanFolders(Path startFolder) {

        List<Path> foldersWithMedia = new ArrayList<>();

        try {

            Files.walkFileTree(startFolder, new SimpleFileVisitor<>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir,
                                                         BasicFileAttributes attrs)
                        throws IOException {

                    // Skip hidden folders early
                    if (Files.isHidden(dir)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir,
                                                          IOException exc)
                        throws IOException {

                    // Skip system folders if needed
                    if (ValidatePathUtils.isInSkippedFolderList(dir)) {
                        return FileVisitResult.CONTINUE;
                    }

                    // IMPORTANT: folder is included if:
                    // - it has media files directly
                    // - OR any subfolder had media (already processed)

                    if (ValidatePathUtils.hasMediaFilesInFolder(dir)) {
                        foldersWithMedia.add(dir);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file,
                                                       IOException exc) {
                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return foldersWithMedia;
    }

}
