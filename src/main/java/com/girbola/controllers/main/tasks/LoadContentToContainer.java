package com.girbola.controllers.main.tasks;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.Model_main;
import com.girbola.controllers.main.Populate;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LoadContentToContainer extends Task<Integer> {

    private static Logger log = LoggerFactory.getLogger(LoadContentToContainer.class);

    private Model_main model_main;

    private IntegerProperty total = new SimpleIntegerProperty();

    private AtomicInteger processAtomicInteger = new AtomicInteger(0);

    public LoadContentToContainer(Model_main model_main) {
        this.model_main = model_main;
        ConcurrencyUtils.initSingleExecutionService();
    }

    @Override
    protected Integer call() throws Exception {



        return null;
    }
}
