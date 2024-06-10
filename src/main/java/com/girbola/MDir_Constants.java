package com.girbola;
/*
 * Constants
 */
public enum MDir_Constants {

	//======== Styling css file names=================
	MAINSTYLE("mainStyle.css"),
	IMAGEVIEWER("imageViewer.css"),
	FOLDERCHOOSER("folderChooser.css"),
	DATEFIXER("dateFixer.css"),
	DIALOGS("dialogs.css"),
	VLCPLAYER("vlcPlayer.css"),
	LOADINGPROCESS("loadingprocess.css"),
	DIALOGSSTYLE("dialogsStyle.css"),
	OPTIONPANE( "option_pane.css");

	//======== Styling css file names============ ENDS
	
	MDir_Constants(String type) {
		this.type = type;
	}

	private String type;

	public String getType() {
		return this.type;
	}

}
