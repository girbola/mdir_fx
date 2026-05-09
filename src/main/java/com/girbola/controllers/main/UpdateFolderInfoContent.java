
package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.controllers.main.tables.TableUtils;
import com.girbola.controllers.main.tables.model.FolderInfo;
import com.girbola.fileinfo.FileInfo;
import com.girbola.messages.Messages;
import com.girbola.misc.Misc;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import static com.girbola.Main.simpleDates;
import static com.girbola.utils.FileInfoUtils.createFileInfo;
import static common.utils.FileUtils.supportedMediaFormat;

public class UpdateFolderInfoContent extends Task<Integer> {

    private static final String ERROR = UpdateFolderInfoContent.class.getSimpleName();

    private final FolderInfo folderInfo;

    public UpdateFolderInfoContent(FolderInfo folderInfo) {
        this.folderInfo = folderInfo;
    }

    @Override
    protected Integer call() {
        Messages.sprintf("Running UpdateFolderInfoContent: " + folderInfo.getFolderPath());

        List<FileInfo> fileInfoSnapshot;

        synchronized (folderInfo) {
            if (folderInfo.getFileInfoList() == null) {
                Messages.sprintf("UpdateFolderInfoContent - Somehow fileInfo list was null!!!");
                Main.setProcessCancelled(true);
                Messages.errorSmth(ERROR, "", null, Misc.getLineNumber(), true);
                cancel();
                return 0;
            }

            fileInfoSnapshot = new ArrayList<>(folderInfo.getFileInfoList());
            try {
                DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(folderInfo.getFolderPath()));
                for (FileInfo fileInfo : folderInfo.getFileInfoList()) {

                    paths.forEach(path -> {
                        if (supportedMediaFormat(path)) {
                            if (!fileInfo.getOrgPath().equals(path.toString())) {
                                try {
                                    FileInfo newFileInfo = createFileInfo(path);
                                    folderInfo.getFileInfoList().add(newFileInfo);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                            }
                        }
                    });
                }
            } catch (IOException ex) {
                Messages.errorSmth(ERROR, "Failed to access folder: " + folderInfo.getFolderPath(), ex, Misc.getLineNumber(), true);
                return null;
            }
        }

        FolderInfoStatusResult result = calculateStatus(fileInfoSnapshot, folderInfo.getFolderPath());

        if (isCancelled() || Main.getProcessCancelled()) {
            return 0;
        }

        updateValue(result);
        return result.folderFiles();
    }

    @Override
    protected void succeeded() {
        super.succeeded();

        FolderInfoStatusResult result = getValueObject();

        if (result == null) {
            return;
        }

        synchronized (folderInfo) {
            folderInfo.setBadFiles(result.badFiles());
            folderInfo.setConfirmed(result.confirmedFiles());
            folderInfo.setCopied(result.copiedFiles());
            folderInfo.setFolderFiles(result.folderFiles());
            folderInfo.setFolderImageFiles(result.imageFiles());
            folderInfo.setFolderRawFiles(result.rawFiles());
            folderInfo.setFolderSize(result.sizeOfFiles());
            folderInfo.setFolderVideoFiles(result.videoFiles());
            folderInfo.setGoodFiles(result.goodFiles());
            folderInfo.setConnected(result.connected());
            folderInfo.setMinDate(result.minDate());
            folderInfo.setMaxDate(result.maxDate());
            folderInfo.setDateDifferenceRatio(result.dateDifferenceRatio());
        }

        Messages.sprintf("UpdateFolderInfoContent succeeded: " + folderInfo.getFolderPath());
    }

    @Override
    protected void cancelled() {
        Messages.sprintf("UpdateFolderInfoContent cancelled: " + folderInfo.getFolderPath());
    }

    @Override
    protected void failed() {
        Throwable exception = getException();
        if (exception != null) {
            Messages.sprintfError("UpdateFolderInfoContent failed: " + folderInfo.getFolderPath() + " error: " + exception.getMessage());
        } else {
            Messages.sprintfError("UpdateFolderInfoContent failed: " + folderInfo.getFolderPath());
        }
    }

    private FolderInfoStatusResult valueObject;

    private void updateValue(FolderInfoStatusResult result) {
        this.valueObject = result;
    }

    private FolderInfoStatusResult getValueObject() {
        return valueObject;
    }

    private FolderInfoStatusResult calculateStatus(List<FileInfo> fileInfoList, String folderPath) {
        int badFiles = 0;
        int goodFiles = 0;
        int imageFiles = 0;
        int rawFiles = 0;
        int videoFiles = 0;
        int confirmedFiles = 0;
        long sizeOfFiles = 0;
        int copiedFiles = 0;
        int ignoredFiles = 0;

        TreeMap<LocalDate, Integer> dateMap = new TreeMap<>();
        List<Long> dateCounterList = new ArrayList<>();

        for (FileInfo fileInfo : fileInfoList) {
            if (isCancelled() || Main.getProcessCancelled()) {
                cancel();
                break;
            }

            if (fileInfo.isIgnored() || fileInfo.isTableDuplicated()) {
                Messages.sprintf("FileInfo was ignored or duplicated: " + fileInfo.getOrgPath());
                ignoredFiles++;
                continue;
            }

            sizeOfFiles += fileInfo.getSize();

            if (fileInfo.isCopied()) {
                copiedFiles++;
            }
            if (fileInfo.isBad()) {
                badFiles++;
            }
            if (fileInfo.isConfirmed()) {
                confirmedFiles++;
            }
            if (fileInfo.isGood()) {
                goodFiles++;
            }
            if (fileInfo.isRaw()) {
                rawFiles++;
            }
            if (fileInfo.isImage()) {
                imageFiles++;
            }
            if (fileInfo.isVideo()) {
                videoFiles++;
            }

            long date = fileInfo.getDate();

            if (date != 0) {
                dateCounterList.add(date);

                LocalDate localDate = createLocalDate(date);
                if (localDate != null) {
                    dateMap.put(localDate, 0);
                }
            }
        }

        long min = 0;
        long max = 0;

        if (!dateCounterList.isEmpty()) {
            Collections.sort(dateCounterList);
            min = Collections.min(dateCounterList);
            max = Collections.max(dateCounterList);
        }

        String minDate = formatDate(min);
        String maxDate = formatDate(max);
        boolean connected = Files.exists(Paths.get(folderPath));
        double dateDifferenceRatio = TableUtils.calculateDateDifferenceRatio(dateMap);
        int folderFiles = imageFiles + rawFiles + videoFiles;

        Messages.sprintf("SIZES: " + sizeOfFiles
                + " Copied: " + copiedFiles
                + " files: " + folderFiles
                + " ignoredFiles: " + ignoredFiles);

        return new FolderInfoStatusResult(
                badFiles,
                goodFiles,
                imageFiles,
                rawFiles,
                videoFiles,
                confirmedFiles,
                sizeOfFiles,
                copiedFiles,
                folderFiles,
                connected,
                minDate,
                maxDate,
                dateDifferenceRatio
        );
    }

    private LocalDate createLocalDate(long date) {
        try {
            synchronized (simpleDates) {
                return LocalDate.of(
                        Integer.parseInt(simpleDates.getSdf_Year().format(date)),
                        Integer.parseInt(simpleDates.getSdf_Month().format(date)),
                        Integer.parseInt(simpleDates.getSdf_Day().format(date))
                );
            }
        } catch (Exception ex) {
            Messages.errorSmth(ERROR, "", ex, Misc.getLineNumber(), true);
            return null;
        }
    }

    private String formatDate(long date) {
        synchronized (simpleDates) {
            return simpleDates.getSdf_ymd_hms_minusDots_default().format(date);
        }
    }

    private record FolderInfoStatusResult(
            int badFiles,
            int goodFiles,
            int imageFiles,
            int rawFiles,
            int videoFiles,
            int confirmedFiles,
            long sizeOfFiles,
            int copiedFiles,
            int folderFiles,
            boolean connected,
            String minDate,
            String maxDate,
            double dateDifferenceRatio
    ) {
    }
}