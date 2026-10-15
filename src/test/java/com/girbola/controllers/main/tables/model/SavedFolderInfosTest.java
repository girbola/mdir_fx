package com.girbola.controllers.main.tables.model;

import com.girbola.drive.DriveInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedFolderInfosTest {

    @Test
    void matchesDriveBySerialAndPhysicalDiskSize() {
        SavedFolderInfos saved = new SavedFolderInfos(
                "E:\\Media", "sorted", "Media", true,
                "disk-id", "disk-serial", 1_000L, "", "");
        DriveInfo current = new DriveInfo(
                "F:\\", 1_000L, true, false, "disk-id", "DISK-SERIAL");

        assertTrue(saved.matchesDrive(current));
    }

    @Test
    void partitionUuidMustNotMatchAnotherPartitionOnTheSameDisk() {
        SavedFolderInfos saved = new SavedFolderInfos(
                "E:\\Media", "sorted", "Media", true,
                "partition-a", "disk-serial", 1_000L, "partition-a", "partition-id-a");
        DriveInfo current = new DriveInfo(
                "F:\\", 1_000L, true, false, "partition-b", "disk-serial");
        current.setPartitionUuid("partition-b");
        current.setPartitionIdentification("partition-id-b");

        assertFalse(saved.matchesDrive(current));
    }

    @Test
    void diskSizeMismatchRejectsAnOtherwiseMatchingSerial() {
        SavedFolderInfos saved = new SavedFolderInfos(
                "E:\\Media", "sorted", "Media", true,
                "disk-id", "disk-serial", 1_000L, "", "");
        DriveInfo current = new DriveInfo(
                "F:\\", 2_000L, true, false, "disk-id", "disk-serial");

        assertFalse(saved.matchesDrive(current));
    }
}
