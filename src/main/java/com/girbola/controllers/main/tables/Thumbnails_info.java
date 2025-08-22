package com.girbola.controllers.main.tables;

import java.nio.file.Path;

public class Thumbnails_info {

	private Path path;

	private int id;
	private byte[] normal;
	private byte[] large;
	private Path imageOrgPath;
	
	Thumbnails_info(int id, Path imageOrgPath) {
		this.id=id;
		this.imageOrgPath=imageOrgPath;
	}
	public final Path getPath() {
		return path;
	}

	public final void setPath(Path path) {
		this.path = path;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public byte[] getNormal() {
		return normal;
	}

	public void setNormal(byte[] normal) {
		this.normal = normal;
	}

	public byte[] getLarge() {
		return large;
	}

	public void setLarge(byte[] large) {
		this.large = large;
	}

}
