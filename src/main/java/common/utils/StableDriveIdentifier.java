package common.utils;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.util.*;

public class StableDriveIdentifier {
    public static class DriveId {
        public final String fsUuid;       // Filesystem/partition UUID
        public final String diskSerial;   // Physical disk serial (may be blank)
        public final String volumeGuid;   // Windows: \\?\Volume{GUID}\ (optional)
        public final String model;
        public final String name;

        public DriveId(String fsUuid, String diskSerial, String volumeGuid, String model, String name) {
            this.fsUuid = fsUuid;
            this.diskSerial = diskSerial;
            this.volumeGuid = volumeGuid;
            this.model = model;
            this.name = name;
        }

        @Override public String toString() {
            return "DriveId{fsUuid=" + fsUuid + ", diskSerial=" + diskSerial +
                    ", volumeGuid=" + volumeGuid + ", model=" + model + ", name=" + name + "}";
        }
    }

    public static Map<String, DriveId> buildMountToStableId() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        Map<String, DriveId> map = new LinkedHashMap<>();

        boolean isWindows = os.getFamily().toLowerCase(Locale.ROOT).contains("windows");

        for (HWDiskStore disk : hal.getDiskStores()) {
            String diskSerial = Optional.ofNullable(disk.getSerial()).orElse("");
            String model = Optional.ofNullable(disk.getModel()).orElse("");
            String name = Optional.ofNullable(disk.getName()).orElse("");
            for (HWPartition p : disk.getPartitions()) {
                String mount = Optional.ofNullable(p.getMountPoint()).orElse("").trim();
                String fsUuid = Optional.ofNullable(p.getUuid()).orElse("").trim();

                if (!mount.isEmpty()) {
                    // Windows: also resolve Volume GUID path for the letter (optional step)
                    String volumeGuid = null;
                    if (isWindows) {
                        // Minimal, portable approach: use Java’s File.listRoots and OSHI mount mapping.
                        // If you want exact GUIDs, call a small JNI/JNA wrapper to GetVolumeNameForVolumeMountPoint.
                        // Here we leave volumeGuid null to keep this snippet 100% pure Java.
                    }

                    // Prefer fsUuid; fall back to disk serial if UUID is missing.
                    String key = mount; // e.g., "D:\" on Windows, "/Volumes/MyDrive" on macOS, "/media/usb" on Linux
                    map.put(key, new DriveId(fsUuid, diskSerial, volumeGuid, model, name));
                }
            }
        }
        return map;
    }

//    public static void main(String[] args) {
//        Map<String, DriveId> m = buildMountToStableId();
//        m.forEach((mount, id) -> System.out.println(mount + " -> " + id));
//    }
}

