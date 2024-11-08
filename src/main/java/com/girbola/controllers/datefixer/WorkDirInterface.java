package com.girbola.controllers.datefixer;

import com.girbola.fileinfo.*;
import java.nio.file.*;
import java.util.*;

public interface WorkDirInterface {

    public boolean loadWorkDirDatabase(Path folder);
    public void saveWorkDirDatabase();
    public void insertFileInfo(FileInfo fileInfo);
    public boolean deleteFileInfo(FileInfo fileInfo);

    public List<FileInfo> findDuplicateByExactDate(FileInfo fileInfo);
    public List<FileInfo> findDuplicateByDateRange(FileInfo fileInfo, String date1, String date2);

    public List<FileInfo> findByExactDate(FileInfo fileInfo, String date);
    public List<FileInfo> findByDateRange(FileInfo fileInfo, String date1, String date2, String date3);

}
