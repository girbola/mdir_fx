package com.girbola.controllers.main;

import com.girbola.Main;
import com.girbola.app.ApplicationContext;
import com.girbola.configuration.Configuration;
import com.girbola.SceneSwitcher;
import java.util.ResourceBundle;

final class LegacyMainApplicationContext implements ApplicationContext {

    @Override
    public Configuration getConfiguration() {
        return Main.conf;
    }

    @Override
    public ResourceBundle getBundle() {
        return Main.bundle;
    }

    @Override
    public SceneSwitcher getSceneManager() {
        return Main.sceneManager;
    }

    @Override
    public boolean isChanged() {
        return Main.getChanged();
    }

    @Override
    public void setChanged(boolean changed) {
        Main.setChanged(changed);
    }

    @Override
    public boolean isProcessCancelled() {
        return Main.getProcessCancelled();
    }

    @Override
    public void setProcessCancelled(boolean processCancelled) {
        Main.setProcessCancelled(processCancelled);
    }
}
