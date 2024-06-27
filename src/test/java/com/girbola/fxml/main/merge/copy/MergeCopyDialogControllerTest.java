package com.girbola.fxml.main.merge.copy;


import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergeCopyDialogControllerTest {

    @Test
    public void testDefinePathByEventLocationUserName() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocationUserName(absolutePath, locationName, eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEventUserName() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocationUserName(absolutePath, locationName, "", userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyLocationUserName() {
        String absolutePath = "C:\\girbola";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocationUserName(absolutePath, "", eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEventAndLocationUserName() {
        String absolutePath = "C:\\girbola";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocationUserName(absolutePath, "", "", userName);

        Path expected = Paths.get(absolutePath + userName);
        assertEquals(expected, actual);
    }
}