/*
 @(#)Copyright:  Copyright (c) 2012-2019 All right reserved. 
 @(#)Author:     Marko Lokka
 @(#)Product:    Image and Video Files Organizer Tool
 @(#)Purpose:    To help to organize images and video files in your harddrive with less pain
 */
package com.girbola.configuration.xml;

import java.io.File;
import java.io.FileOutputStream;
import javafx.concurrent.Task;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.girbola.controllers.main.tables.FolderInfo;

import static com.girbola.Main.conf;
import static com.girbola.messages.Messages.sprintf;

/**
 *
 * @author Marko Lokka
 */
public class SaveXMLToFolder extends Task<Void> {

    private FolderInfo folderInfo;

    public SaveXMLToFolder(FolderInfo aFolderInfo) {
        this.folderInfo = aFolderInfo;
    }

    @Override
    protected Void call() throws Exception {
        try {

            FileOutputStream file = new FileOutputStream(folderInfo.getFolderPath() + File.separator + conf.getFolderInfo_FileName(), false);
            sprintf("FileOutputStream: " + (folderInfo.getFolderPath() + File.separator + conf.getFolderInfo_FileName()));

            JAXBContext jaxbContext = JAXBContext.newInstance(FolderInfoWrapper.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            FolderInfoWrapper wrapper = new FolderInfoWrapper();
            wrapper.setFolderInfo(folderInfo);

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(wrapper, file);
//            jaxbMarshaller.marshal(wrapper, System.out);

        } catch (Exception e) {
            cancel();
            sprintf("Failed to write XML file to folder. " + e.getMessage());
        }
        return null;
    }
}
