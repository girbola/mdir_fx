package com.girbola.app;

import com.girbola.configuration.Configuration;
import com.girbola.SceneSwitcher;
import java.util.ResourceBundle;

public interface ApplicationContext {

    Configuration getConfiguration();

    ResourceBundle getBundle();

    SceneSwitcher getSceneManager();

    boolean isChanged();

    void setChanged(boolean changed);

    boolean isProcessCancelled();

    void setProcessCancelled(boolean processCancelled);
}


