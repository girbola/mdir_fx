/*
 @(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.controllers.main;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.girbola.filelisting.ValidatePathUtils;

import javafx.concurrent.Task;

/**
 *
 * @author Marko Lokka
 */
public class GetSubs extends Task<List<Path>> {

    private List<Path> list = new ArrayList<>();
    private Path p;

    public GetSubs(Path p) {
        this.p = p;
    }

    private void calculate(Path p) throws IOException {
        DirectoryStream<Path> ds = Files.newDirectoryStream(p);
        for (Path path : ds) {
            if (ValidatePathUtils.validFolder(path)) {
                list.add(path);
                calculate(path);
            }
        }
    }

    @Override
    protected List<Path> call() throws Exception {
        calculate(p);
        return list;
    }

}
