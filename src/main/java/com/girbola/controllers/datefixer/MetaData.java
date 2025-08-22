package com.girbola.controllers.datefixer;

import javafx.beans.property.SimpleStringProperty;

public class MetaData {
	private SimpleStringProperty tag;
	private SimpleStringProperty value;

	public MetaData(String tag) {
		this.tag = new SimpleStringProperty(tag);
		this.value = new SimpleStringProperty();
	}

	public MetaData(String tag, String value) {
		this.tag = new SimpleStringProperty(tag);
		this.value = new SimpleStringProperty(value);
	}

	/**
	 * @return the fileName_property
	 */
	public SimpleStringProperty fileName_property() {
		return tag;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setTag(SimpleStringProperty fileName) {
		this.tag = fileName;
	}

	/**
	 * @return the fileName
	 */
	public String getTag() {
		return this.tag.get();
	}

	/**
	 * @return the value
	 */
	public SimpleStringProperty value_property() {
		return value;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return this.value.get();
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value.set(value);
	}
}
