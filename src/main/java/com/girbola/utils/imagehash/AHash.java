package com.girbola.utils.imagehash;


// AHash.java

import java.awt.*;
import java.awt.image.BufferedImage;

public class AHash {
    public static long aHash(BufferedImage img) {
        BufferedImage resized = resize(img, 8, 8);
        long hash = 0L;
        int[] pixels = new int[64];
        int idx = 0;
        int sum = 0;

        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++) {
                int gray = getGray(resized.getRGB(x, y));
                pixels[idx++] = gray;
                sum += gray;
            }

        int avg = sum / 64;

        for (int g : pixels) {
            hash <<= 1;
            if (g > avg) hash |= 1;
        }
        return hash;
    }

    private static int getGray(int rgb) {
        Color c = new Color(rgb);
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }

    private static BufferedImage resize(BufferedImage img, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}
