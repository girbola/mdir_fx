package com.girbola;
/*
 * Constants
 */
public enum MDir_Constants {

	//======== Styling css file names=================
	MAINSTYLE("mainStyle.css"),
	DATEFIXER("dateFixer.css"),
	DIALOGS("dialogs.css");
	//======== Styling css file names============ ENDS
	
	MDir_Constants(String type) {
		this.type = type;
	}

	private String type;

	public String getType() {
		return this.type;
	}

}
