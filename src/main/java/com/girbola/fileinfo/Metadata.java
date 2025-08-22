package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Metadata extends FileMetadata {
	private double width;
	private double height;
    private String fileName;

}
