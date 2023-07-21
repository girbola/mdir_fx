package com.girbola.fileinfo;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;

@Getter
@Setter
@XmlRootElement(name = "ThumbInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class ThumbInfo {

	private int id;
	private double thumb_width;
	private double thumb_height;
	private double thumb_fast_width;
	private double thumb_fast_height;
	private double orientation;
	private String fileName;
	private byte[] thumb_fast;
	private ArrayList<byte[]> thumbs;

	public ThumbInfo(String aFileName, int id) {
		this.fileName = aFileName;
		this.id = id;
		this.thumbs = new ArrayList<>();
	}

	//	public ThumbInfo() {
	//		this(null, 0);
	//	}
	public ThumbInfo() {
		this(
				0,
				null,
				0,
				0,
				0,
				0,
				0,
				null);
	}

	public ThumbInfo(int id,
			String aFileName,
			double thumb_width,
			double thumb_height,
			double thumb_fast_width,
			double thumb_fast_height,
			double orientation,
			ArrayList<byte[]> thumbs) {
		this.id = id;
		this.fileName = aFileName;
		this.thumb_height = thumb_height;
		this.thumb_width = thumb_width;
		this.thumb_fast_height = thumb_fast_height;
		this.thumb_fast_width = thumb_fast_width;
		this.orientation = orientation;
		this.thumbs = thumbs;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ThumbInfo [id=" + id + ", thumb_width=" + thumb_width + ", thumb_height=" + thumb_height + ", thumb_fast_width="
				+ thumb_fast_width + ", thumb_fast_height=" + thumb_fast_height + ", orientation=" + orientation + ", fileName="
				+ fileName + ", thumb_fast=" + Arrays.toString(thumb_fast) + ", thumbs size=" + thumbs.size() + "]";
	}

}
