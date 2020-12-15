package com.girbola.fileinfo;

public enum FileInfoState {
	BAD("bad"), GOOD("good"), MODIFIED("modified"), IGNORED("ignored");

	private String type;

	FileInfoState(String type) {
	        this.type = type;
	    }

	public String getType() {
		return type;
	}

}
