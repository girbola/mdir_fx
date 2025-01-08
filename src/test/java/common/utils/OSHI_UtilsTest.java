package common.utils;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OSHI_UtilsTest {

    /**
     * Test class for OSHI_Utils
     * Method under test is getDriveSerialNumber. This method retrieves the serial number of the drive based on a provided path
     */

    @Test
    public void shouldReturnNullWhenPathIsNotAMountPoint() {
        // Given
        String path = "P:/non/existing/path";
        HWDiskStore disk = mock(HWDiskStore.class);
        when(disk.getPartitions()).thenReturn(Collections.emptyList());

        SystemInfo si = mock(SystemInfo.class);
        HardwareAbstractionLayer hal = mock(HardwareAbstractionLayer.class);
        when(si.getHardware()).thenReturn(hal);
        when(hal.getDiskStores()).thenReturn(Collections.singletonList(disk));

        // When
        String serial = OSHI_Utils.getDriveSerialNumber(path);

        // Then
        assertNull(serial);
    }

    @Test
    public void getSerialNumber() {
        String serial = OSHI_Utils.getDriveSerialNumber("C:");
        System.out.println("serial: " + serial);
    }

    @Test
    public void getDrive() {
        HWDiskStore disk = OSHI_Utils.getDrive("C:");
        System.out.println("disk name: " + disk.getName());
        System.out.println("disk.getPartitions(): " + disk.getPartitions().size());
        System.out.println("disk.getTimeStamp(): " + disk.getTimeStamp());
        assertNotNull(disk);
    }

}