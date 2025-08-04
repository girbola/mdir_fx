
package com.girbola.controllers.folderscanner;

import javafx.beans.property.*;


public class FolderInfoTable {

    private StringProperty path;
    private IntegerProperty folders;
    private IntegerProperty files;
    private BooleanProperty checked;
    private IntegerProperty media;

    public FolderInfoTable(String path) {
        this.path = new SimpleStringProperty(path);
        this.folders = new SimpleIntegerProperty(0);
        this.files = new SimpleIntegerProperty(0);
        this.checked = new SimpleBooleanProperty(false);
        this.media = new SimpleIntegerProperty(0);
//        File[] list = new File(path).listFiles();
//        int folder = 0;
//
//        findFiles(new File(path));
    }

    public void clear() {
        setFolders(0);
        setFiles(0);
        setMedia(0);
        
    }
    public BooleanProperty checkedProperty() {
        return checked;
    }

    public IntegerProperty folderProperty() {
        return folders;
    }

    public IntegerProperty filesProperty() {
        return files;
    }

    public IntegerProperty mediaProperty() {
        return media;
    }

    public StringProperty pathProperty() {
        return this.path;
    }

    public boolean getChecked() {
        return this.checked.get();
    }

    public void setChecked(boolean value) {
        this.checked.set(value);
    }

    public String getPath() {
        return this.path.get();
    }

    public void setPath(String value) {
        this.path.set(value);
    }

    public int getFolders() {
        return this.folders.get();
    }

    public void setFolders(int folders) {
        this.folders.set(folders);
    }

    public int getMedia() {
        return this.media.get();
    }

    public void setMedia(int value) {
        this.media.set(value);
    }

    public int getFiles() {
        return this.files.get();
    }

    public void setFiles(int files) {
        this.files.set(files);
    }

}
