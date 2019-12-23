package com.girbola.eventinfo;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class EventInfo {
	private SimpleStringProperty event;
	private SimpleLongProperty minDate;
	private SimpleLongProperty maxDate;

	public EventInfo(String event, long minDate, long maxDate) {
		super();
		this.event = new SimpleStringProperty(event);
		this.minDate = new SimpleLongProperty(minDate);
		this.maxDate = new SimpleLongProperty(maxDate);
	}

	public EventInfo() {

	}

	public SimpleStringProperty event_property() {
		return event;
	}

	public SimpleLongProperty minDate_property() {
		return minDate;
	}

	public SimpleLongProperty maxDate_property() {
		return maxDate;
	}

	public String getEvent() {
		return event.get();
	}

	public long getMinDate() {
		return minDate.get();
	}

	public long getMaxDate() {
		return maxDate.get();
	}

	public void setEvent(String event) {
		this.event.set(event);
	}

	public void setMinDate(long minDate) {
		this.minDate.set(minDate);
	}

	public void setMaxDate(long maxDate) {
		this.maxDate.set(maxDate);
	}

}
