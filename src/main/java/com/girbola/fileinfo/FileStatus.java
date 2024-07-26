package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileStatus extends MediaInformation {
    private boolean bad;
    private boolean confirmed;
    private boolean copied;
    private boolean good;
    private boolean ignored;
    private boolean suggested;
    private boolean tableDuplicated;
}
