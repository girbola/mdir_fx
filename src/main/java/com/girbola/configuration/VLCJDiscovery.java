package com.girbola.configuration;

import com.girbola.Main;
import com.girbola.messages.Messages;
import javafx.scene.control.Alert.AlertType;
import uk.co.caprica.vlcj.binding.support.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.Info;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

import java.nio.file.Path;

public class VLCJDiscovery {

	private static void checkVlcPlayerVersion() {
		Messages.sprintf("getLib started");
		LibVlcVersion lbl = new LibVlcVersion();
		if (lbl.getRequiredVersion().atLeast(lbl.getVersion())) {
			Messages.sprintf("" + Main.bundle.getString("vlcPlayerVersionIsOld") + " ver: " + lbl.getVersion() + " req: " + lbl.getRequiredVersion());
			Messages.showAlert(Main.bundle.getString("vlcPlayerVersionIsOld"), AlertType.WARNING);
			Messages.sprintf("getRequiredVersion " + lbl.getRequiredVersion());
			Main.conf.setVlcSupport(false);
		}

	}

	public static boolean discovery(Path path) {
		Messages.sprintf("disvocery started: " + path);
		NativeDiscovery dis = new NativeDiscovery() {
			@Override
			protected void onFound(String path, NativeDiscoveryStrategy strategy) {
				Messages.sprintf("Found; " + " path: " + path + " strategy: " + strategy);
			}
			@Override
			protected void onNotFound() {
				Messages.sprintf("Native not found");
			}
		};
		/*	if (path != null) {
				Messages.sprintf("Path were not null");
				if (Files.exists(path)) {
					Messages.sprintf("Path were existed");
					NativeLibrary.addSearchPath("libvlc", Main.conf.getVlcPath());
					Messages.sprintf("addSearchPath done");
				}
			} else {
				String discoPath = dis.discoveredPath();
				Messages.sprintf("Path disco: " + discoPath);
			}*/
		//		String discoPath = dis.discoveredPath();
		Messages.sprintf("2Path VLC discovery: ");
		boolean found = dis.discover(); //new NativeDiscovery().discover();
		Messages.sprintf("Found?: " + found);
		if (found) {
			Messages.sprintf("VLC found? " + found + " discoveredPath: " + dis.discoveredPath());
			Main.conf.setVlcPath(dis.discoveredPath());
//			checkVlcPlayerVersion();
			return true;
		} else {
			Messages.sprintf("No VLC Found");
			return false;
		}
	}
	public static void initVlc() {
		boolean found = discovery(null);
		if (found) {
			Messages.sprintf("Found");
		} else {
			Messages.sprintf("Not Found");
		}
		Info info = Info.getInstance();

		Messages.sprintf("vlcj             : %s%n", info.vlcjVersion() != null ? info.vlcjVersion() : "<version not available>");
		Messages.sprintf("os               : %s%n", (info.os()));
		Messages.sprintf("java             : %s%n", (info.javaVersion()));
		Messages.sprintf("java.home        : %s%n", (info.javaHome()));
		Messages.sprintf("jna.library.path : %s%n", (info.jnaLibraryPath()));
		Messages.sprintf("java.library.path: %s%n", (info.javaLibraryPath()));
		Messages.sprintf("PATH             : %s%n", (info.path()));
		Messages.sprintf("VLC_PLUGIN_PATH  : %s%n", (info.pluginPath()));

		if (RuntimeUtil.isNix()) {
			Messages.sprintf(" LD_LIBRARY_PATH  : %s%n", (info.ldLibraryPath()));
		} else if (RuntimeUtil.isMac()) {
			Messages.sprintf("DYLD_LIBRARY_PATH          : %s%n", (info.dyldLibraryPath()));
			Messages.sprintf("DYLD_FALLBACK_LIBRARY_PATH : %s%n", (info.dyldFallbackLibraryPath()));
		}
	}


}
