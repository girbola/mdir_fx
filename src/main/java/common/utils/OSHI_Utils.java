package common.utils;

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
                if (part.getMountPoint().equals(path)) {
//					Messages.sprintf("Disk serial is: " + disk.getSerial());
                    return disk.getSerial();
                }
            }
        }
        return null;
    }
}
