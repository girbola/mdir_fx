package com.girbola.controllers.datefixer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Iterator;

public class ObservableHandler {
	private ObservableList<String> event_obs = FXCollections.observableArrayList();
	private ObservableList<String> location_obs = FXCollections.observableArrayList();
	private ObservableList<String> user_obs = FXCollections.observableArrayList();

	public void addIfExists(String obs, String value) {
		Iterator<String> it = null;
		if (obs.equals(ObservabeleListType.EVENT.getType())) {
			it = event_obs.iterator();
		} else if (obs.equals(ObservabeleListType.LOCATION.getType())) {
			it = location_obs.iterator();
		}
		while (it.hasNext()) {
			String current = it.next();
			if (current.equals(value)) {
				return;
			}
		}
		if (obs.equals(ObservabeleListType.EVENT.getType())) {
			event_obs.add(value);
		} else if (obs.equals(ObservabeleListType.LOCATION.getType())) {
			location_obs.add(value);
		}
	}

	public ObservableList<String> getEvent_obs() {
		return this.event_obs;
	}

	public ObservableList<String> getLocation_obs() {
		return this.location_obs;
	}

	public ObservableList<String> getUser_obs() {
		return this.user_obs;
	}
	
	enum ObservabeleListType {
		EVENT("event"), LOCATION("location");

		private String type;

		private ObservabeleListType(String type) {
			this.type = type;
		}

		String getType() {
			return this.type;
		}

	}
}
