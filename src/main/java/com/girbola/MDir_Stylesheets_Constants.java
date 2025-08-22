package com.girbola;
/*
 * Constants
 */
public enum MDir_Stylesheets_Constants {

	//======== Styling css file names=================
	DATEFIXER("dateFixer.css"),
	DIALOGS("dialogs.css"),
	DIALOGSSTYLE("dialogsStyle.css"),
	FOLDERCHOOSER("folderChooser.css"),
	IMAGEVIEWER("imageViewer.css"),
	LOADINGPROCESS("loadingprocess.css"),
	MAINSTYLE("mainStyle.css"),
	MODENA("modena/modena.css"),
	OPTIONPANE( "option_pane.css"),
	VLCPLAYER("vlcPlayer.css");

	//======== Styling css file names============ ENDS
	
	MDir_Stylesheets_Constants(String type) {
		this.type = type;
	}

	private String type;

	public String getType() {
		return this.type;
	}

}
