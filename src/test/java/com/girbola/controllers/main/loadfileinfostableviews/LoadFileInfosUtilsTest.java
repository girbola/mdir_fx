package com.girbola.controllers.main.loadfileinfostableviews;

import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadFileInfosUtilsTest {

    @Test
    void leavesNewStateWhenNoEligibleFilesWereCopied() {
        FolderInfo folderInfo = folderInfoWithFiles(fileInfo(false), fileInfo(false));

        assertFalse(LoadFileInfosUtils.recoverProcessState(folderInfo));
        assertEquals("NEW", folderInfo.getProcessState());
    }

    @Test
    void marksPartialCopyAsInterruptedAndExcludesIgnoredAndDuplicateFiles() {
        FileInfo copied = fileInfo(true);
        FileInfo notCopied = fileInfo(false);
        FileInfo ignored = fileInfo(true);
        ignored.setIgnored(true);
        FileInfo duplicate = fileInfo(false);
        duplicate.setTableDuplicated(true);
        FolderInfo folderInfo = folderInfoWithFiles(copied, notCopied, ignored, duplicate);

        assertTrue(LoadFileInfosUtils.recoverProcessState(folderInfo));
        assertEquals("INTERRUPTED", folderInfo.getProcessState());
    }

    @Test
    void marksDoneWhenAllEligibleFilesWereCopied() {
        FileInfo copied = fileInfo(true);
        FileInfo ignored = fileInfo(false);
        ignored.setIgnored(true);
        FolderInfo folderInfo = folderInfoWithFiles(copied, ignored);

        assertTrue(LoadFileInfosUtils.recoverProcessState(folderInfo));
        assertEquals("DONE", folderInfo.getProcessState());
    }

    @Test
    void doesNotChangeAProcessStateThatIsNotNew() {
        FolderInfo folderInfo = folderInfoWithFiles(fileInfo(true));
        folderInfo.setProcessState("FAILED");

        assertFalse(LoadFileInfosUtils.recoverProcessState(folderInfo));
        assertEquals("FAILED", folderInfo.getProcessState());
    }

    private static FolderInfo folderInfoWithFiles(FileInfo... fileInfos) {
        FolderInfo folderInfo = new FolderInfo();
        folderInfo.setFileInfoList(new ArrayList<>(Arrays.asList(fileInfos)));
        return folderInfo;
    }

    private static FileInfo fileInfo(boolean copied) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setCopied(copied);
        return fileInfo;
    }
}
