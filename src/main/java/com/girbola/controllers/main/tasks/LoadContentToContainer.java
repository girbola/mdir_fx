package com.girbola.controllers.main.tasks;

import com.girbola.concurrency.ConcurrencyUtils;
import com.girbola.controllers.main.ModelMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class LoadContentToContainer extends Task<Integer> {

    private static Logger log = LoggerFactory.getLogger(LoadContentToContainer.class);

    private ModelMain model_main;

    private IntegerProperty total = new SimpleIntegerProperty();

    private AtomicInteger processAtomicInteger = new AtomicInteger(0);

    public LoadContentToContainer(ModelMain model_main) {
        this.model_main = model_main;
        ConcurrencyUtils.initNewSingleExecutionService();
    }

    @Override
    protected Integer call() throws Exception {



        return null;
    }
}
