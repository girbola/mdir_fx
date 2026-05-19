package com.girbola.controllers.folderscanner.searchservice;

import common.utils.FileUtils;
import javafx.concurrent.Task;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

public class FolderSearchService {

    public Task<Set<Path>> createFolderSearchTask(String rootFolderPath) {
        return new Task<>() {
            @Override
            protected Set<Path> call() throws Exception {
                Path rootDir = Paths.get(rootFolderPath);
                Set<Path> foldersWithMedia = new HashSet<>();

                if (!Files.exists(rootDir) || !Files.isDirectory(rootDir)) {
                    throw new IllegalArgumentException("Virheellinen juurikansio");
                }

                // Käydään tiedostopuu läpi tehokkaasti walkFileTree-metodilla
                Files.walkFileTree(rootDir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (FileUtils.supportedMediaFormat(file)) {
                            Path parent = file.getParent();
                            if (parent != null) {
                                foldersWithMedia.add(parent);
                                // OPTIMOINTI: Koska kansio sisältää jo mediaa,
                                // ohitetaan kansion loput tiedostot ja siirrytään seuraavaan kansioon.
                                return FileVisitResult.SKIP_SIBLINGS;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                        // Ohitetaan kansiot/tiedostot, joihin ei ole käyttöoikeutta (esim. järjestelmätiedostot)
                        return FileVisitResult.CONTINUE;
                    }
                });

                return foldersWithMedia;
            }
        };
    }

}
