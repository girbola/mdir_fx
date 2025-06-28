package common.utils;

import com.girbola.messages.*;
import java.nio.file.*;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;

public class OSHI_Utils {

    public static String getDriveSerialNumber(String path) {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        for (HWDiskStore disk : hal.getDiskStores()) {
            for (HWPartition part : disk.getPartitions()) {
                Path root = Paths.get(path).getRoot();
                if (part.getMountPoint().contains(root.toString())) {
					Messages.sprintf("Disk serial is: " + disk.getSerial());
                    return disk.getSerial();
                }
            }
        }
        return null;
    }

    public static HWDiskStore getDrive(String path) {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        for (HWDiskStore disk : hal.getDiskStores()) {
            for (HWPartition part : disk.getPartitions()) {
                Path root = Paths.get(path).getRoot();
                if (part.getMountPoint().contains(root.toString())) {
                    Messages.sprintf("Disk s: " + disk.toString());
                    return disk;
                }
            }
        }
        return null;
    }
}
