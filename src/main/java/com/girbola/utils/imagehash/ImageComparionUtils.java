package com.girbola.utils.imagehash;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

public class ImageComparionUtils {
    public static long pHash(BufferedImage img) {
        BufferedImage small = resize(img, 32, 32);
        double[][] vals = new double[32][32];

        for (int y = 0; y < 32; y++)
            for (int x = 0; x < 32; x++)
                vals[x][y] = getGray(small.getRGB(x, y));

        double[][] dct = applyDCT(vals);
        double[] low = new double[64];

        int idx = 0;
        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 8; x++)
                low[idx++] = dct[x][y];

        java.util.Arrays.sort(low);
        double median = low[32];

        long hash = 0L;
        for (double v : low) {
            hash <<= 1;
            if (v > median) hash |= 1;
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

    public static double[][] applyDCT(double[][] f) {
        int N = 32;
        double[][] F = new double[N][N];

        for (int u = 0; u < N; u++)
            for (int v = 0; v < N; v++) {
                double sum = 0;
                for (int i = 0; i < N; i++)
                    for (int j = 0; j < N; j++)
                        sum += f[i][j]
                                * Math.cos(((2*i+1)*u*Math.PI)/(2*N))
                                * Math.cos(((2*j+1)*v*Math.PI)/(2*N));

                double cu = (u==0)?1/Math.sqrt(2):1;
                double cv = (v==0)?1/Math.sqrt(2):1;
                F[u][v] = 0.25*cu*cv*sum;
            }
        return F;
    }

    public static String computePHash(String imagePath) {
        // 1. Load image
        Mat img = opencv_imgcodecs.imread(imagePath);
        if (img.empty()) {
            return null;
        }

        // 2. Resize to 32x32
        Mat resized = new Mat();
        opencv_imgproc.resize(img, resized, new Size(32, 32));

        // 3. Convert to grayscale
        Mat gray = new Mat();
        opencv_imgproc.cvtColor(resized, gray, opencv_imgproc.COLOR_BGR2GRAY);

        // 4. Convert to float (required for DCT)
        Mat floatImg = new Mat();
        gray.convertTo(floatImg, opencv_core.CV_32F);

        // 5. Apply DCT
        Mat dct = new Mat();
        opencv_core.dct(floatImg, dct);

        // 6. Take top-left 8x8 block (low frequencies)
        int size = 8;
        double[][] dctVals = new double[size][size];
        double sum = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                double val = dct.ptr(i, j).getFloat();
                dctVals[i][j] = val;
                sum += val;
            }
        }

        // 7. Compute average (excluding DC coefficient [0][0] optional)
        double avg = (sum - dctVals[0][0]) / (size * size - 1);

        System.out.println("Average: " + avg);
        System.out.println("SUM: " + sum);
        // 8. Build hash
        long hash = 0L;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i == 0 && j == 0) continue; // skip DC term
                hash <<= 1;
                if (dctVals[i][j] > avg) hash |= 1L;
            }
        }

        return Long.toHexString(hash);
    }

    public static int hammingDistance(String hash1, String hash2) {
        int dist = 0;
        for (int i = 0; i < hash1.length(); i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                dist++;
            }
        }
        return dist;
    }

    public static double pixelSimilarity(Path path1, Path path2, int width, int height) throws IOException {
        if (path1 == null || path2 == null) {
            return 0.0;
        }

        BufferedImage img1 = ImageIO.read(path1.toFile());
        BufferedImage img2 = ImageIO.read(path2.toFile());


        BufferedImage resized1 = resizeToGray(img1, width, height);
        BufferedImage resized2 = resizeToGray(img2, width, height);

        int total = width * height;
        int equal = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p1 = resized1.getRGB(x, y) & 0xFF;
                int p2 = resized2.getRGB(x, y) & 0xFF;

                if (p1 == p2) {
                    equal++;
                }
            }
        }

        return (equal * 100.0) / total;
    }

    private static BufferedImage resizeToGray(BufferedImage img, int width, int height) {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, width, height, null);
        g.dispose();
        return out;
    }

    public static BufferedImage createDifferenceImage(Path path1, Path path2) throws IOException {
        if (path1 == null || path2 == null) {
            return null;
        }

        BufferedImage img1 = ImageIO.read(path1.toFile());
        BufferedImage img2 = ImageIO.read(path2.toFile());

        if (img1 == null || img2 == null) {
            return null;
        }

        int width = Math.min(img1.getWidth(), img2.getWidth());
        int height = Math.min(img1.getHeight(), img2.getHeight());

        BufferedImage gray1 = resizeToGray(img1, width, height);
        BufferedImage gray2 = resizeToGray(img2, width, height);

        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p1 = gray1.getRGB(x, y) & 0xFF;
                int p2 = gray2.getRGB(x, y) & 0xFF;

                int delta = Math.abs(p1 - p2);
                int value = Math.min(255, delta * 4);

                int rgb = new Color(value, value, value).getRGB();
                diff.setRGB(x, y, rgb);
            }
        }

        return diff;
    }

}

