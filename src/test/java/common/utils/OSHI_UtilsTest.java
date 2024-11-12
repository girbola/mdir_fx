package common.utils;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

public class OSHI_UtilsTest {

    /**
     * Test class for OSHI_Utils
     * Method under test is getDriveSerialNumber. This method retrieves the serial number of the drive based on a provided path
     */

    @Test
    public void shouldReturnDiskSerialWhenPathIsAMountPoint() {
        // Given
        String expectedSerial = "EXPECTED_SERIAL";
        String path = "c:\\Temppi\\";
        HWPartition partition = mock(HWPartition.class);
        when(partition.getMountPoint()).thenReturn(path);

        HWDiskStore disk = mock(HWDiskStore.class);
        when(disk.getPartitions()).thenReturn(Collections.singletonList(partition));
        when(disk.getSerial()).thenReturn(expectedSerial);

        SystemInfo si = mock(SystemInfo.class);
        HardwareAbstractionLayer hal = mock(HardwareAbstractionLayer.class);
        when(si.getHardware()).thenReturn(hal);
        when(hal.getDiskStores()).thenReturn(Collections.singletonList(disk));

        // When
        String actualSerial = OSHI_Utils.getDriveSerialNumber(path);

        // Then
        assertEquals(expectedSerial, actualSerial);
    }

    @Test
    public void shouldReturnNullWhenPathIsNotAMountPoint() {
        // Given
        String path = "/non/existing/path";
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

}