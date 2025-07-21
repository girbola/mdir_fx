
package com.girbola.filelisting;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 * Get all possible media files under given folder
 */
public class CheckMediaExistenceInFolder {
    private final static String ERROR = GetAllMediaFiles.class.getSimpleName();

    public static boolean getAllMediaFiles(Path path) {

        AllMediaFilesConcurrency fv = new AllMediaFilesConcurrency();

        try {
            Files.walkFileTree(path, fv);
            return fv.getFileFound();
        } catch (IOException ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
        }
        return fv.getFileFound();
    }
}

class AllMediaFilesConcurrency extends SimpleFileVisitor<Path> {

    private boolean fileFound;

    public boolean getFileFound() {
        return fileFound;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
        return FileVisitResult.SKIP_SIBLINGS;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
        return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        try {
            if (ValidatePathUtils.validFile(file)) {
                fileFound = true;
                return FileVisitResult.TERMINATE;
            }
        } catch (IOException ex) {
            return SKIP_SUBTREE;
        }
        return CONTINUE;
    }
}
