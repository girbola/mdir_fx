package com.girbola.utils.imagehash;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DHash {
    public static long dHash(BufferedImage img) {
        BufferedImage resized = resize(img, 9, 8);
        long hash = 0L;

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int left = getGray(resized.getRGB(x, y));
                int right = getGray(resized.getRGB(x + 1, y));
                hash <<= 1;
                if (left < right) hash |= 1L;
            }
        }
        return hash;
    }

    private static int getGray(int rgb) {
        Color c = new Color(rgb, true);
        return (c.getRed() + c.getGreen() + c.getBlue()) / 3;
    }

    private static BufferedImage resize(BufferedImage img, int w, int h) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();
        return out;
    }
}