package com.girbola.fxml.main.merge.copy;


import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MergeCopyDialogControllerTest {

    @Test
    public void testDefinePathByEventLocation() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocation(absolutePath, locationName, eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEvent() {
        String absolutePath = "C:\\girbola";
        String locationName = "location";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocation(absolutePath, locationName, "", userName);

        Path expected = Paths.get(absolutePath + " - " + locationName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyLocation() {
        String absolutePath = "C:\\girbola";
        String eventName = "event";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocation(absolutePath, "", eventName, userName);

        Path expected = Paths.get(absolutePath + " - " + eventName + userName);
        assertEquals(expected, actual);
    }

    @Test
    public void testDefinePathByEventLocation_emptyEventAndLocation() {
        String absolutePath = "C:\\girbola";
        String userName = "user";

        Path actual = MergeCopyDialogController.definePathByEventLocation(absolutePath, "", "", userName);

        Path expected = Paths.get(absolutePath + userName);
        assertEquals(expected, actual);
    }
}