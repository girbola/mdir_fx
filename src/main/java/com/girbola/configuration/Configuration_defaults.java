
package com.girbola.configuration;

import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static com.girbola.Main.bundle;
import static com.girbola.messages.Messages.sprintf;


public class Configuration_defaults extends
		ConfigurationWindow {

	private final String ERROR = Configuration_defaults.class.getSimpleName();

	private StringProperty drive_name = new SimpleStringProperty("");
	private StringProperty drive_space = new SimpleStringProperty("");
	private StringProperty drive_spaceLeft = new SimpleStringProperty("");
	private BooleanProperty drive_connected = new SimpleBooleanProperty(false);

	private boolean vlcSupport = false;
	private boolean saveDataToHD = false;

	public static final String programName = "Mdir";

	private final String programVersion = "0.0.2020";
	private final LocalDate programDate = LocalDate.now();

	private AtomicInteger id_counter = new AtomicInteger(0);
	private final String os = System.getProperty("os.name").toLowerCase();
	private final String os_arch = System.getProperty("os.arch").toLowerCase();
	private final String os_data_model = System.getProperty("sun.arch.data.model").toLowerCase();
	private final String os_version = System.getProperty("os.version").toLowerCase();
	private StringProperty vlcPath = new SimpleStringProperty("");

	public static final float VLC_THUMBNAIL_POSITION = 30.0f / 100.0f;
	public static final String[] VLC_ARGS = { "--intf", "dummy", /* no interface */
			"--vout", "dummy", /* we don't want video (output) */
			"--no-audio", /* we don't want audio (decoding) */
			"--no-osd", "--no-spu", "--no-stats", /* no stats */
			"--no-sub-autodetect-file", /* we don't want subtitles */
			// "--no-inhibit", /* we don't want interfaces */
			"--no-disable-screensaver", /* we don't want interfaces */
			"--no-snapshot-preview", /* no blending in dummy vout */ };

	public StringProperty drive_name_property() {
		return drive_name;
	}
	public String getDrive_name() {
		return this.drive_name.get();
	}

	public void setDrive_name(String drive_name) {
		this.drive_name.set(drive_name);
	}

	public StringProperty drive_space_property() {
		return drive_space;
	}
	public String getDrive_space() {
		return drive_space.get();
	}

	public void setDrive_space(String drive_space) {
		this.drive_space.set(drive_space);
	}

	public StringProperty drive_spaceLeft_property() {
		return this.drive_spaceLeft;
	}
	public String getDrive_spaceLeft() {
		return this.drive_spaceLeft.get();
	}

	public void setDrive_spaceLeft(String drive_spaceLeft) {
		this.drive_spaceLeft.set(drive_spaceLeft);
	}
	
	public BooleanProperty drive_connected_property() {
		return this.drive_connected;
	}
	public Boolean getDrive_connected() {
		return this.drive_connected.get();
	}

	public void setDrive_connected(boolean drive_connected) {
		this.drive_connected.set(drive_connected);
	}
	public boolean isSaveDataToHD() {
		return saveDataToHD;
	}

	public void setSaveDataToHD(boolean saveDataToHD) {
		this.saveDataToHD = saveDataToHD;
	}
	public boolean isVlcSupport() {
		return vlcSupport;
	}

	public void setVlcSupport(boolean vlcSupport) {
		this.vlcSupport = vlcSupport;
	}

	public String getProgramVersion() {
		return this.programVersion;
	}

	public LocalDate getProgramDate() {
		return this.programDate;
	}

	public StringProperty vlcPath_property() {
		return this.vlcPath;
	}

	public String getVlcPath() {
		return this.vlcPath.get();
	}

	public void setVlcPath(String vlcPath) {
		this.vlcPath.set(vlcPath);
	}

	public void createProgramPaths() {
		// Check& create program dat path
		Path appDataPath = getAppDataPath();
		sprintf("Creating appDataPath os: " + appDataPath);
		if (!Files.exists(appDataPath)) {
			try {
				sprintf("appDataPath created: " + getAppDataPath());
				Files.createDirectories(appDataPath);

			} catch (IOException ex) {
				if (Files.isWritable(appDataPath)) {
					Messages.sprintf("Current user might not have permission to write into path: " + appDataPath);
					Messages.errorSmth(ERROR, bundle.getString("createDataFolderFailed") + "\n" + getAppDataPath(), ex, Misc.getLineNumber(), true);
					return;
				} else {
					Messages.errorSmth(ERROR,
							bundle.getString("createDataFolderFailed") + " " + bundle.getString("folderWriteProtected") + "\n" + getAppDataPath(), ex,
							Misc.getLineNumber(), true);
					return;
				}
			}
		}
	}

	public String getOs() {
		return this.os;
	}

	public String getOs_arch() {
		return this.os_arch;
	}

	public String getOs_data_model() {
		return this.os_data_model;
	}

	public String getOs_version() {
		return this.os_version;
	}

	/**
	 * @return the id_counter
	 */
	public AtomicInteger getId_counter() {
		return id_counter;
	}
	public void setId_counter(int value) {
		this.id_counter.set(value);
	}

}
