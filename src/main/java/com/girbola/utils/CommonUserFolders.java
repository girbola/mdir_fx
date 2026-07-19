package com.girbola.utils;

// File: CommonUserFolders.java
// Cross-platform resolver for Documents, Pictures, Downloads.
// Windows: uses Known Folders API (JNA) – correct under any locale/redirection.
// macOS: assumes ~/Documents, ~/Pictures, ~/Downloads.
// Linux: reads ~/.config/user-dirs.dirs (XDG) with fallback to ~/… if missing.

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonUserFolders {

    public static String getCommonOSUserFolders() {
        Map<Kind, Path> resolveCommonOsUserFolders = resolve();
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<Kind, Path> entry : resolveCommonOsUserFolders.entrySet()) {
            Kind k = entry.getKey();
            Path p = entry.getValue();
            System.out.println(k + " -> " + p);
            sb.append(k).append(" -> ").append(p).append(" ");
        }
        return sb.toString().trim();
    }

    public enum Kind { DOCUMENTS, PICTURES, DOWNLOADS, VIDEOS }

    public static Map<Kind, Path> resolve() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        if (os.contains("win"))  return resolveWindows();
        if (os.contains("mac"))  return resolveMac();
        return resolveLinux();
    }

    // -------------------- Windows (Known Folders via JNA) --------------------
    // If you cannot add JNA, see the "Windows fallback" section at the bottom.

    private static Map<Kind, Path> resolveWindows() {
        Map<Kind, Path> map = new EnumMap<>(Kind.class);

        try {
            Path docs = WindowsKnownFolders.getKnownFolder(WindowsKnownFolders.FOLDERID_Documents);
            Path pics = WindowsKnownFolders.getKnownFolder(WindowsKnownFolders.FOLDERID_Pictures);
            Path dlds = WindowsKnownFolders.getKnownFolder(WindowsKnownFolders.FOLDERID_Downloads);
            Path videos = WindowsKnownFolders.getKnownFolder(WindowsKnownFolders.FOLDERID_Videos);

            map.put(Kind.DOCUMENTS, docs);
            map.put(Kind.PICTURES,  pics);
            map.put(Kind.DOWNLOADS, dlds);
            map.put(Kind.VIDEOS,    videos);
            return map;
        } catch (Throwable t) {
            // Fallback: user.home + English names (still works on most locales)
            Path home = Paths.get(System.getProperty("user.home"));
            map.put(Kind.DOCUMENTS, home.resolve("Documents"));
            map.put(Kind.PICTURES,  home.resolve("Pictures"));
            map.put(Kind.DOWNLOADS, home.resolve("Downloads"));
            map.put(Kind.VIDEOS,    home.resolve("Videos"));
            return map;
        }
    }

    // -------------------- macOS --------------------
    private static Map<Kind, Path> resolveMac() {
        Map<Kind, Path> map = new EnumMap<>(Kind.class);
        Path home = Paths.get(System.getProperty("user.home"));

        // On disk these are almost always English; Finder localizes display names.
        map.put(Kind.DOCUMENTS, home.resolve("Documents"));
        map.put(Kind.PICTURES,  home.resolve("Pictures"));
        map.put(Kind.DOWNLOADS, home.resolve("Downloads"));
        map.put(Kind.VIDEOS,    home.resolve("Movies"));
        return map;
    }

    // -------------------- Linux (XDG user-dirs) --------------------
    private static Map<Kind, Path> resolveLinux() {
        Map<Kind, Path> map = new EnumMap<>(Kind.class);
        Path home = Paths.get(System.getProperty("user.home"));
        Path xdg = home.resolve(".config").resolve("user-dirs.dirs");

        Map<String, Path> vars = readXdgUserDirs(xdg, home);

        map.put(Kind.DOCUMENTS, vars.getOrDefault("XDG_DOCUMENTS_DIR", home.resolve("Documents")));
        map.put(Kind.PICTURES,  vars.getOrDefault("XDG_PICTURES_DIR",  home.resolve("Pictures")));
        map.put(Kind.DOWNLOADS, vars.getOrDefault("XDG_DOWNLOAD_DIR",  home.resolve("Downloads")));
        map.put(Kind.VIDEOS,    vars.getOrDefault("XDG_VIDEOS_DIR",    home.resolve("Videos")));
        return map;
    }

    private static Map<String, Path> readXdgUserDirs(Path file, Path home) {
        Map<String, Path> out = new HashMap<>();
        if (!Files.isRegularFile(file)) return out;

        Pattern pat = Pattern.compile("^(XDG_[A-Z_]+)=(\"?)(.+)\\2$"); // key="value" or key=value
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                Matcher m = pat.matcher(line);
                if (m.matches()) {
                    String key = m.group(1);
                    String raw = m.group(3).replace("\"", "");
                    String expanded = raw.replace("$HOME", home.toString());
                    Path p = Paths.get(expanded).toAbsolutePath().normalize();
                    out.put(key, p);
                }
            }
        } catch (IOException ignored) {}
        return out;
    }

    // -------- Convenience: test if a path is one of these dirs (or inside) --------
    public static Optional<Kind> classify(Path candidate) {
        Map<Kind, Path> dirs = resolve();
        Path abs = candidate.toAbsolutePath().normalize();
        for (Map.Entry<Kind, Path> e : dirs.entrySet()) {
            Path root = e.getValue().toAbsolutePath().normalize();
            if (abs.equals(root) || abs.startsWith(root)) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    private CommonUserFolders() {}
}
