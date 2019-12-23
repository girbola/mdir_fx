package com.girbola.controllers.main.tables;

import java.io.IOException;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FolderInfo_DOM {
	/*
	 * private Integer badFiles;
	 * private Double dateDifference;
	 * private Integer folderFiles;
	 * private Integer folderImageFiles;
	 * private Integer folderRawFiles;
	 * private Integer folderVideoFiles;
	 * private Integer goodFiles;
	 * private Integer copied;
	 * private Integer suggested;
	 * private Long folderSize;
	 * private String folderJustPathName;
	 * private String folderPath;
	 * private String maxDate;
	 * private String minDate;
	 * private String state;
	 * private Integer status;
	 * 
	 * 
	 */
	private Path path;

	public FolderInfo_DOM(Path path) {
		this.path = path;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder;

			builder = factory.newDocumentBuilder();

			Document document = builder.parse(path.toString());
			document.getDocumentElement().normalize();

			Element root = document.getDocumentElement();

			NodeList nodeList = document.getElementsByTagName("fileinfo");
			for (int i = 0; i < nodeList.getLength(); i++) {

			}

		} catch (ParserConfigurationException | SAXException | IOException e) {

			e.printStackTrace();
		}
	}
}
