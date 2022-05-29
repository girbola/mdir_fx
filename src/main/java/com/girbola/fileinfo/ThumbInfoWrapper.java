package com.girbola.fileinfo;
/*
@(#)Copyright:  Copyright (c) 2012-2022 All right reserved. 
@(#)Author:     Marko Lokka
@(#)Product:    Image and Video Files Organizer Tool
@(#)Purpose:    To help to organize images and video files in your harddrive with less pain
*/

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ThumbInfo")
public class ThumbInfoWrapper {

	private List<ThumbInfo> thumbInfo_list;

	/**
	 * 
	 * @return
	 */
	@XmlElement(name = "thumbInfo")
	public List<ThumbInfo> getThumbInfo_list() {
		return thumbInfo_list;
	}

	/**
	 * 
	 * @param thumbInfo_list
	 */
	public void setThumbInfo_list(List<ThumbInfo> thumbInfo_list) {
		this.thumbInfo_list = thumbInfo_list;
	}
}
