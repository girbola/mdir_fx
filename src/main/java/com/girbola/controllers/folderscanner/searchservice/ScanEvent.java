package com.girbola.controllers.folderscanner.searchservice;

import java.nio.file.Path;

public record ScanEvent(Path path, ScanEvent.Type type) {

    public enum Type {
        FOUND_FOLDER,
        FOUND_FILE,
        DONE,
        ERROR
    }
}