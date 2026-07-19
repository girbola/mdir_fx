package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaInformation extends FileDetails {
    private String camera_model;
    private String imageDifferenceHash;
    private String user;
    private boolean image;
    private boolean raw;
    private boolean video;
    private int thumb_length;
    private int thumb_offset;
}
