package com.girbola.controllers.importimages;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class ToggleButtonControl {

	private BooleanProperty start_toggled = new SimpleBooleanProperty();

	private BooleanProperty end_toggled = new SimpleBooleanProperty();

	public boolean getStart_toggled() {
		return start_toggled.get();
	}

	public boolean getEnd_toggled() {
		return end_toggled.get();
	}

	public void setStart_toggled(boolean start_toggled) {
		this.start_toggled.set(start_toggled);
	}

	public void setEnd_toggled(boolean end_toggled) {
		this.end_toggled.set(end_toggled);
	}

	public BooleanProperty start_toggled_property() {
		return start_toggled;
	}

	public BooleanProperty end_toggled_property() {
		return end_toggled;
	}
}
