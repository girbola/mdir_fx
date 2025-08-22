package com.girbola.sql;

import com.girbola.drive.DriveInfo;
import java.sql.Connection;
import java.util.List;

public interface SQLInterface {
    public boolean create();
    public boolean delete();
    public boolean insert();
    public Connection getConfigurationConnection();
    public boolean isConnected();
    List<DriveInfo> load();
    public boolean save(List<DriveInfo> driveInfos);
    public boolean update(List<DriveInfo> driveInfo);
}
