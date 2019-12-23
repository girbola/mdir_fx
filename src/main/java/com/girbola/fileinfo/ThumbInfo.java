package com.girbola.fileinfo;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

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

	public String getFileName() {
		return this.fileName;
	}

	public int getId() {
		return this.id;
	}

	/**
	 * @return the thumbs
	 */
	public ArrayList<byte[]> getThumbs() {
		return thumbs;
	}

	/**
	 * @return the width
	 */
	public double getThumb_width() {
		return this.thumb_width;
	}

	/**
	 * @return the height
	 */
	public double getThumb_height() {
		return this.thumb_height;
	}

	/**
	 * @param value the width to set
	 */
	public void setThumb_width(double value) {
		this.thumb_width = value;
	}

	/**
	 * @param value the height to set
	 */
	public void setThumb_height(double value) {
		this.thumb_height = value;
	}

	/**
	 * @param value the thumbs to set
	 */
	public void setThumbs(ArrayList<byte[]> value) {
		this.thumbs = value;
	}

	/**
	 * @return the thumb_fast
	 */
	public final byte[] getThumb_fast() {
		return thumb_fast;
	}

	/**
	 * @param thumb_fast the thumb_fast to set
	 */
	public final void setThumb_fast(byte[] thumb_fast) {
		this.thumb_fast = thumb_fast;
	}

	/**
	 * @return the thumb_fast_width
	 */
	public final double getThumb_fast_width() {
		return thumb_fast_width;
	}

	/**
	 * @return the thumb_fast_height
	 */
	public final double getThumb_fast_height() {
		return thumb_fast_height;
	}

	/**
	 * @param thumb_fast_width the thumb_fast_width to set
	 */
	public final void setThumb_fast_width(double thumb_fast_width) {
		this.thumb_fast_width = thumb_fast_width;
	}

	/**
	 * @param thumb_fast_height the thumb_fast_height to set
	 */
	public final void setThumb_fast_height(double thumb_fast_height) {
		this.thumb_fast_height = thumb_fast_height;
	}

	/**
	 * @return the orientation
	 */
	public final double getOrientation() {
		return orientation;
	}

	/**
	 * @param orientation the orientation to set
	 */
	public final void setOrientation(double orientation) {
		this.orientation = orientation;
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
