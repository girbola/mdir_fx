package com.girbola.sql;

public interface SQLInterface {
    public boolean save();
    public boolean load();
    public boolean delete();
    public boolean update();
    public boolean create();
    public boolean insert();
    public boolean isConnected();
}
