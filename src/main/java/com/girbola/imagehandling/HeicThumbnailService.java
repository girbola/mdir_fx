package com.girbola.imagehandling;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class HeicThumbnailService {

    // Change to instance members if they are state-dependent, 
    // or keep static constants if they are configuration
    private final List<String> TRUSTED_PATHS = List.of(
            "/usr/bin",
            "/usr/local/bin",
            "/opt/homebrew/bin",
            "C:\\Program Files\\ffmpeg\\bin",
            "C:\\ffmpeg\\bin"
    );

    /**
     * OPTIONAL: Known-good hashes.
     * Leave empty to disable strict hash enforcement.
     */
    public static final Set<String> KNOWN_HASHES = Set.of(
// Example:
// "f3d0e9b6e0d8b65d7e6f3c1e6f6d4c2b3a1eabc1234567890abcdef123456789"
    );

    /**
     * If true → fail if hash not in KNOWN_HASHES
     * If false → just log hash (recommended default)
     */
    public static boolean STRICT_HASH_CHECK = false;

    static class FfmpegInfo {
        String path;
        String version;
        boolean supportsHevc;
        String sha256;
        int score;
    }

    // -----------------------------
// PUBLIC API
// -----------------------------

    public boolean isFfmpegAvailable() {
        try {
            return !discoverAndInspect().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    // Remove 'static' from createThumbnail, discoverAndInspect, etc.
    public void createThumbnail(String input, String output) throws Exception {
        List<FfmpegInfo> candidates = discoverAndInspect();

        if (candidates.isEmpty()) {
            throw new RuntimeException("No usable ffmpeg found");
        }

        for (FfmpegInfo f : candidates) {
            try {
                System.out.println("\nTrying ffmpeg: " + f.path);
                System.out.println("Hash: " + f.sha256);

                runFfmpeg(f.path, input, output);

                System.out.println("✅ Success with: " + f.path);
                return;

            } catch (Exception e) {
                System.out.println("❌ Failed with: " + f.path);
            }
        }

        throw new RuntimeException("All ffmpeg candidates failed");
    }

    // -----------------------------
// DISCOVERY
// -----------------------------
    // Change private static to private (instance)
    private List<FfmpegInfo> discoverAndInspect() throws Exception {
        List<String> paths = findAllFfmpegPaths();
        List<FfmpegInfo> result = new ArrayList<>();

        for (String path : paths) {
            try {
                if (!isTrusted(path)) continue;

                FfmpegInfo info = inspect(path);

                if (isUsable(info)) {

// ✅ Compute SHA-256
                    info.sha256 = sha256(path);

// ✅ Validate hash
                    validateHash(info);

                    info.score = score(info);
                    result.add(info);
                }

            } catch (Exception e) {
                System.out.println("Skipping " + path + ": " + e.getMessage());
            }
        }

        result.sort((a, b) -> Integer.compare(b.score, a.score));
        return result;
    }

    // -----------------------------
// FIND BINARIES
// -----------------------------
    // Change static to instance
    List<String> findAllFfmpegPaths() throws Exception {
        List<String> results = new ArrayList<>();

        List<String> command = isWindows()
                ? List.of("where", "ffmpeg")
                : List.of("which", "-a", "ffmpeg");

        Process p = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        String line;
        while ((line = reader.readLine()) != null) {
            results.add(line.trim());
        }

        p.waitFor();

        return results;
    }

    // -----------------------------
// INSPECT
// -----------------------------
    // Change public static to public (instance)
    public FfmpegInfo inspect(String path) throws Exception {
        FfmpegInfo info = new FfmpegInfo();
        info.path = path;

        Process p = new ProcessBuilder(path, "-version")
                .redirectErrorStream(true)
                .start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        info.version = reader.readLine();
        p.waitFor();

        Process p2 = new ProcessBuilder(path, "-codecs")
                .redirectErrorStream(true)
                .start();

        BufferedReader r2 = new BufferedReader(
                new InputStreamReader(p2.getInputStream()));

        info.supportsHevc = r2.lines()
                .anyMatch(line -> line.toLowerCase().contains("hevc"));

        p2.waitFor();

        return info;
    }

    // -----------------------------
// VALIDATION
// -----------------------------
    private boolean isUsable(FfmpegInfo f) {
        if (f.version == null) return false;
        if (!f.version.toLowerCase().contains("ffmpeg")) return false;
        if (!f.supportsHevc) return false;
        return true;
    }

    private boolean isTrusted(String path) {
        return TRUSTED_PATHS.stream().anyMatch(path::startsWith);
    }

    // Change private static to protected/package-private so spy can override it
    void validateHash(FfmpegInfo info) {
        if (KNOWN_HASHES.isEmpty()) {
// Relaxed mode ⇒ just log
            return;
        }

        if (!KNOWN_HASHES.contains(info.sha256)) {
            String msg = "Unapproved ffmpeg binary: " + info.path;

            if (STRICT_HASH_CHECK) {
                throw new SecurityException(msg);
            } else {
                System.out.println("⚠ WARNING: " + msg);
            }
        }
    }

    // -----------------------------
// HASH FUNCTION
// -----------------------------
    private static String sha256(String path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream is = Files.newInputStream(Paths.get(path))) {

            byte[] buffer = new byte[8192];
            int read;

            while ((read = is.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        byte[] hash = digest.digest();

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    // -----------------------------
// SCORING
// -----------------------------
    private static int score(FfmpegInfo f) {
        int score = 0;

        if (f.supportsHevc) score += 10;

        if (f.path.contains("/usr/local/bin")) score += 5;
        if (f.path.contains("/opt/homebrew")) score += 4;
        if (f.path.contains("/usr/bin")) score += 3;
        if (f.path.toLowerCase().contains("program files")) score += 5;

        if (f.version != null && f.version.contains("6.")) score += 2;
        if (f.version != null && f.version.contains("5.")) score += 1;

        return score;
    }

    // -----------------------------
// EXECUTION
// -----------------------------
    public void runFfmpeg(String ffmpegPath, String input, String output) throws Exception {
        Process p = new ProcessBuilder(
                ffmpegPath,
                "-y",
                "-i", input,
                "-frames:v", "1",
                output
        )
                .redirectErrorStream(true)
                .start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()));

        reader.lines().forEach(System.out::println);

        int exit = p.waitFor();

        if (exit != 0) {
            throw new RuntimeException("ffmpeg failed (" + exit + ")");
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    // -----------------------------
// DEMO
// -----------------------------
 /*   public static void main(String[] args) {
        try {
            createThumbnail("input.heic", "thumb.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    } */
}