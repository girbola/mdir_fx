package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileMetadata extends FileStatus {
    private String destination_Path;
    private String event;
    private String location;
    private String tags;
    private int fileInfo_id;
    private int orientation;
}
