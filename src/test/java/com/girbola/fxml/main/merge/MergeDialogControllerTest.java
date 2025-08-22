package com.girbola.fxml.main.merge;


import com.girbola.controllers.main.merge.MergeDialogController;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergeDialogControllerTest {

    @Test
    public void testDefinePathByLocationEventUserName() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeDialogController.definePathByLocationEventUserName(absolutePath, locationName, eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEventUserName() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String userName = "user";

        Path actual = MergeDialogController.definePathByLocationEventUserName(absolutePath, locationName, "", userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyLocationUserName() {
        String absolutePath = "C:\\girbola";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeDialogController.definePathByLocationEventUserName(absolutePath, "", eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEventAndLocationUserName() {
        String absolutePath = "C:\\girbola";
        String userName = "user";

        Path actual = MergeDialogController.definePathByLocationEventUserName(absolutePath, "", "", userName);

        Path expected = Paths.get(absolutePath + userName);
        assertEquals(expected, actual);
    }
}