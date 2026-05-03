package common.utils;

import com.girbola.messages.*;

import java.io.File;
import java.nio.file.*;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;

public class OSHI_Utils {

   public static String getDriveSerialNumber(String path) {
        Path pathRoot = Paths.get(new File(path).getAbsolutePath()).getRoot();
        Messages.sprintf("Path root is: " + pathRoot.toString());
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        for (HWDiskStore disk : hal.getDiskStores()) {
            for (HWPartition part : disk.getPartitions()) {
//                Messages.sprintf("Mount point: " + part.getMountPoint() + " path: " + pathRoot.toString());
                if (part.getMountPoint().contains(pathRoot.toString())) {
//                    Messages.sprintf("disk: " + disk.toString() + " PART: " + part.toString());
                    String uuid = part.getUuid();
//                    Messages.sprintf("Partition uuid: " + uuid);
                    String serial = disk.getSerial();
                    if (serial == null || serial.isEmpty()) {
                        serial = part.getUuid(); // fallback for macOS
                    }
                    return serial != null ? serial : "";
                }
            }
        }
        return "";
    }

    public static HWDiskStore getDrive(String path) {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        for (HWDiskStore disk : hal.getDiskStores()) {
            for (HWPartition part : disk.getPartitions()) {
                Path root = Paths.get(path).getRoot();
                if (part.getMountPoint().contains(root.toString())) {
                    Messages.sprintf("Is root drive: " + disk.toString());
                    return disk;
                }
            }
        }
        return null;
    }
}
