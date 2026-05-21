package com.girbola.controllers.folderscanner;

import com.girbola.controllers.folderscanner.searchservice.FolderSearchService;
import javafx.concurrent.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.Future.State;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class FolderSearchServiceTest {

    private FolderSearchService searchService;

    @BeforeEach
    void setUp() {
        searchService = new FolderSearchService();
    }

    @Test
    void testEtsiKansiotJoissaOnMediaa(@TempDir Path tempDir) throws Exception {
        // Luodaan testirakenne:
        // tempDir/
        // ├── Loma2026/ (sisältää kuvan)
        // ├── Asiakirjat/ (ei sisällä mediaa)
        // │   └── raportti.pdf
        // └── Videot/
        //     └── Alikansio/ (sisältää videon)

        Path lomaKansio = tempDir.resolve("Loma2026");
        Path asiakirjatKansio = tempDir.resolve("Asiakirjat");
        Path videotAlikansio = tempDir.resolve("Videot").resolve("Alikansio");

        Files.createDirectories(lomaKansio);
        Files.createDirectories(asiakirjatKansio);
        Files.createDirectories(videotAlikansio);

        // Luodaan tiedostot
        Files.createFile(lomaKansio.resolve("kuva1.jpg"));
        Files.createFile(lomaKansio.resolve("kuva2.png")); // Sibling-testiä varten
        Files.createFile(asiakirjatKansio.resolve("raportti.pdf"));
        Files.createFile(videotAlikansio.resolve("leffa.mp4"));

        // Luodaan Task
        Task<Set<Path>> task = searchService.createFolderSearchTask(tempDir.toString());

        // Koska Task ajaa koodia taustalla, käytetään Latchia odottamiseen testissä
        CountDownLatch latch = new CountDownLatch(1);

        task.setOnSucceeded(event -> latch.countDown());
        task.setOnFailed(event -> latch.countDown());

        // Käynnistetään testi taustalla
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();

        // Odotetaan enintään 5 sekuntia taustasäikeen valmistumista
        boolean valmistuiAjoissa = latch.await(5, TimeUnit.SECONDS);
        assertTrue(valmistuiAjoissa, "Haku kesti liian kauan");

        // Haetaan tulokset
        Set<Path> tulokset = task.getValue();

        // Tarkistukset (Asetukset)
        assertNotNull(tulokset);
        assertEquals(2, tulokset.size(), "Pitäisi löytää tasan 2 kansiota");
        assertTrue(tulokset.contains(lomaKansio), "Loma-kansion pitäisi löytyä");
        assertTrue(tulokset.contains(videotAlikansio), "Videoiden alikansion pitäisi löytyä");
        assertFalse(tulokset.contains(asiakirjatKansio), "Asiakirjat-kansiota ei pitäisi löytyä");
    }

    @Test
    void testTyhjaKansioPalauttaaTyhjanListan(@TempDir Path tempDir) throws Exception {
        Path tyhjaKansio = tempDir.resolve("Tyhja");
        Files.createDirectories(tyhjaKansio);

        Task<Set<Path>> task = searchService.createFolderSearchTask(tyhjaKansio.toString());
        CountDownLatch latch = new CountDownLatch(1);
        task.setOnSucceeded(event -> latch.countDown());

        new Thread(task).start();
        latch.await(2, TimeUnit.SECONDS);

        Set<Path> tulokset = task.getValue();
        assertTrue(tulokset.isEmpty(), "Tuloksen pitäisi olla tyhjä");
    }

    @Test
    void testVirheellinenPolkuHeittaaPoikkeuksen() {
        // Testataan, että viallinen polku aiheuttaa virheen
        String olematonPolku = "/tätä/polkua/ei/ole/olemassa/12345";

        Task<Set<Path>> task = searchService.createFolderSearchTask(olematonPolku);

        // Koska poikkeus heitetään task.run() / call() sisällä,
        // se ei tule suoraan metodikutsusta vaan Taskin sisäisestä tilasta
        task.run();

        assertEquals(Future.State.FAILED, task.getState(), "Taskin pitäisi epäonnistua");
        assertNotNull(task.getException());
        assertTrue(task.getException() instanceof IllegalArgumentException);
    }
}
