package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileDetails {
    private String orgPath;
    private String workDir;
    private String workDirDriveSerialNumber;
    private long date;
    private long size;
}
