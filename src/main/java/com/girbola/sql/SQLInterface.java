package com.girbola.sql;

import java.sql.Connection;

public interface SQLInterface {
    public boolean create();
    public boolean delete();
    public boolean insert();
    public Connection getConfigurationConnection();
    public boolean isConnected();
    public boolean load();
    public boolean save();
    public boolean update();
}
