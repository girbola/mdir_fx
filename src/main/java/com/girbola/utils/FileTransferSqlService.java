package com.girbola.utils;

import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.persistence.fileinfo.FileInfoDao;
import com.girbola.sql.SQL_Utils;
import java.nio.file.Path;
import java.sql.Connection;

public class FileTransferSqlService {

    public boolean handleCopySuccess(
            Connection sourceConnection,
            Connection targetConnection,
            FileInfo sourceFileInfo,
            Path targetFilePath) {

        try {
            SQL_Utils.setAutoCommit(sourceConnection, false);
            SQL_Utils.setAutoCommit(targetConnection, false);

            FileInfo targetFileInfo = buildTargetFileInfo(sourceFileInfo, targetFilePath);

            if (!insertTargetFileInfo(targetConnection, targetFileInfo)) {
                SQL_Utils.rollBackConnection(sourceConnection);
                SQL_Utils.rollBackConnection(targetConnection);
                return false;
            }

            copyThumbInfoIfMissing(sourceConnection, targetConnection, sourceFileInfo, targetFileInfo);

            SQL_Utils.commitChanges(sourceConnection);
            SQL_Utils.commitChanges(targetConnection);
            return true;

        } catch (Exception e) {
            Messages.sprintfError("handleCopySuccess failed: " + e.getMessage());
            SQL_Utils.rollBackConnection(sourceConnection);
            SQL_Utils.rollBackConnection(targetConnection);
            return false;
        }
    }

    public boolean handleMoveSuccess(
            Connection sourceConnection,
            Connection targetConnection,
            FileInfo sourceFileInfo,
            Path targetFilePath) {

        try {
            SQL_Utils.setAutoCommit(sourceConnection, false);
            SQL_Utils.setAutoCommit(targetConnection, false);

            FileInfo targetFileInfo = buildTargetFileInfo(sourceFileInfo, targetFilePath);

            if (!insertTargetFileInfo(targetConnection, targetFileInfo)) {
                SQL_Utils.rollBackConnection(sourceConnection);
                SQL_Utils.rollBackConnection(targetConnection);
                return false;
            }

            copyThumbInfoIfMissing(sourceConnection, targetConnection, sourceFileInfo, targetFileInfo);

            if (!deleteSourceFileInfo(sourceConnection, sourceFileInfo)) {
                SQL_Utils.rollBackConnection(sourceConnection);
                SQL_Utils.rollBackConnection(targetConnection);
                return false;
            }

            deleteSourceThumbIfOrphan(sourceConnection, sourceFileInfo);

            SQL_Utils.commitChanges(sourceConnection);
            SQL_Utils.commitChanges(targetConnection);
            return true;

        } catch (Exception e) {
            Messages.sprintfError("handleMoveSuccess failed: " + e.getMessage());
            SQL_Utils.rollBackConnection(sourceConnection);
            SQL_Utils.rollBackConnection(targetConnection);
            return false;
        }
    }

    public boolean handleDeleteSuccess(Connection sourceConnection, FileInfo fileInfo) {
        try {
            SQL_Utils.setAutoCommit(sourceConnection, false);

            if (!deleteSourceFileInfo(sourceConnection, fileInfo)) {
                SQL_Utils.rollBackConnection(sourceConnection);
                return false;
            }

            deleteSourceThumbIfOrphan(sourceConnection, fileInfo);

            SQL_Utils.commitChanges(sourceConnection);
            return true;

        } catch (Exception e) {
            Messages.sprintfError("handleDeleteSuccess failed: " + e.getMessage());
            SQL_Utils.rollBackConnection(sourceConnection);
            return false;
        }
    }

    private FileInfo buildTargetFileInfo(FileInfo sourceFileInfo, Path targetFilePath) {
        FileInfo target = new FileInfo();
        target.setBad(sourceFileInfo.isBad());
        target.setCamera_model(sourceFileInfo.getCamera_model());
        target.setConfirmed(sourceFileInfo.isConfirmed());
        target.setCopied(true);
        target.setDate(sourceFileInfo.getDate());
        target.setDestination_Path(sourceFileInfo.getDestination_Path());
        target.setEvent(sourceFileInfo.getEvent());
        target.setFileHistories(sourceFileInfo.getFileHistories());
        target.setFileInfo_id(sourceFileInfo.getFileInfo_id());
        target.setGood(sourceFileInfo.isGood());
        target.setIgnored(sourceFileInfo.isIgnored());
        target.setImage(sourceFileInfo.isImage());
        target.setImageDifferenceHash(sourceFileInfo.getImageDifferenceHash());
        target.setLocation(sourceFileInfo.getLocation());
        target.setModified(sourceFileInfo.isModified());
        target.setOrgPath(targetFilePath.toString());
        target.setOrgPathDriveSerialNumber(sourceFileInfo.getOrgPathDriveSerialNumber());
        target.setOrientation(sourceFileInfo.getOrientation());
        target.setRaw(sourceFileInfo.isRaw());
        target.setSize(sourceFileInfo.getSize());
        target.setSuggested(sourceFileInfo.isSuggested());
        target.setTableDuplicated(sourceFileInfo.isTableDuplicated());
        target.setTags(sourceFileInfo.getTags());
        target.setThumb_length(sourceFileInfo.getThumb_length());
        target.setThumb_offset(sourceFileInfo.getThumb_offset());
        target.setTimeShift(sourceFileInfo.getTimeShift());
        target.setUser(sourceFileInfo.getUser());
        target.setVideo(sourceFileInfo.isVideo());
        target.setWorkDir(sourceFileInfo.getWorkDir());
        target.setWorkDirDriveSerialNumber(sourceFileInfo.getWorkDirDriveSerialNumber());
        return target;
    }

    private boolean insertTargetFileInfo(Connection targetConnection, FileInfo targetFileInfo) {
        return FileInfoDao.createFileInfoTable(targetConnection)
                && insertOneFileInfo(targetConnection, targetFileInfo);
    }

    private boolean insertOneFileInfo(Connection connection, FileInfo fileInfo) {
        // delegate to your existing insert helper / DAO
        return true;
    }

    private boolean deleteSourceFileInfo(Connection sourceConnection, FileInfo fileInfo) {
        // delegate to your existing delete helper / DAO
        return true;
    }

    private void copyThumbInfoIfMissing(
            Connection sourceConnection,
            Connection targetConnection,
            FileInfo sourceFileInfo,
            FileInfo targetFileInfo) {

        // load thumb from source, check if already exists in target, insert only if missing
    }

    private void deleteSourceThumbIfOrphan(Connection sourceConnection, FileInfo fileInfo) {
        // only delete if no remaining fileinfo references the thumb
        Messages.warningText("deleteSourceThumbIfOrphan Not ready yet");
    }
}
