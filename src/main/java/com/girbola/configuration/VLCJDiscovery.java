package com.girbola.configuration;

import java.nio.file.Path;

import com.girbola.Main;
import com.girbola.dialogs.Dialogs;
import com.girbola.messages.Messages;

import javafx.scene.control.Alert.AlertType;
import uk.co.caprica.vlcj.binding.RuntimeUtil;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery;
import uk.co.caprica.vlcj.factory.discovery.strategy.NativeDiscoveryStrategy;
import uk.co.caprica.vlcj.support.Info;
import uk.co.caprica.vlcj.support.version.LibVlcVersion;

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
				System.out.println("Found; " + " path: " + path + " strategy: " + strategy);
			}
			@Override
			protected void onNotFound() {
				System.out.println("Native not found");
			}
		};
		/*	if (path != null) {
				System.out.println("Path were not null");
				if (Files.exists(path)) {
					System.out.println("Path were existed");
					NativeLibrary.addSearchPath("libvlc", Main.conf.getVlcPath());
					System.out.println("addSearchPath done");
				}
			} else {
				String discoPath = dis.discoveredPath();
				Messages.sprintf("Path disco: " + discoPath);
			}*/
		//		String discoPath = dis.discoveredPath();
		Messages.sprintf("2Path disco: ");

		boolean found = dis.discover(); //new NativeDiscovery().discover();
		Messages.sprintf("Found?: " + found);
		if (found) {
			System.out.println("found? " + found + " discoveredPath: " + dis.discoveredPath());
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
