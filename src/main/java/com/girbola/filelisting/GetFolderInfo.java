/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.filelisting;

import static com.girbola.messages.Messages.sprintf;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

/**
 *
 * @author Marko Lokka
 */
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.girbola.Main;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;

/**
 * Finds all folders which contains FolderInfo.xml files including subfolders
 * 
 *
 */
public class GetFolderInfo {

    private final static String ERROR = GetFolderInfo.class.getSimpleName();

    /**
     * getFolderInfoFiles finds all folders which contains FolderInfo.xml files
     * including subfolders
     * 
     * @param path
     * @return
     */
    public static List<Path> getFolderInfoFiles(Path path) {

	GetAllFolderInfos fv = new GetAllFolderInfos();

	List<Path> foundFiles = fv.foundPaths;

	try {
	    Files.walkFileTree(path, fv);
	} catch (IOException ex) {
	    Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
	}
	return foundFiles;
    }
}

class GetAllFolderInfos extends SimpleFileVisitor<Path> {

    public List<Path> foundPaths = new ArrayList<>();

    @Override
    public FileVisitResult postVisitDirectory(Path t, IOException ioe) throws IOException {
	return CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path t, IOException ioe) throws IOException {
	sprintf("DEBUG - getallfiles visitFilefailed getallfiles: " + t);
	return FileVisitResult.SKIP_SIBLINGS;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path t, BasicFileAttributes bfa) throws IOException {
	return CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
	try {
	    if (file.toString().equals(Main.conf.getFolderInfo_FileName())) {
		foundPaths.add(file);
	    }
	} catch (Exception ex) {
	    sprintf("DEBUG - IOException: " + file);
	    return SKIP_SUBTREE;
	}
	return CONTINUE;
    }
}
