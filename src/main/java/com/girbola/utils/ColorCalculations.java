package com.girbola.utils;

import java.io.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javax.imageio.*;
import lombok.*;

@Getter
@Setter
public class ColorCalculations {

    private int[] fixedColor;

    public ColorCalculations(String backGroundColor, String foreGroundColor) {

        int backGroundResultRed = Integer.valueOf(backGroundColor.substring(0, 2), 16);
        int backGroundResultGreen = Integer.valueOf(backGroundColor.substring(2, 4), 16);
        int backGroundResultBlue = Integer.valueOf(backGroundColor.substring(4, 6), 16);

        int foreGroundResultRed = Integer.valueOf(foreGroundColor.substring(0, 2), 16);
        int foreGroundResultGreen = Integer.valueOf(foreGroundColor.substring(2, 4), 16);
        int foreGroundResultBlue = Integer.valueOf(foreGroundColor.substring(4, 6), 16);

        int[] background = {backGroundResultRed, backGroundResultGreen, backGroundResultBlue};
        int[] foreground = {foreGroundResultGreen, foreGroundResultRed, foreGroundResultBlue};

        fixedColor = fixContrast(foreground, background, 4.5);

        // Convert RGB to Color
        Color color = rgbToColor(fixedColor[0], fixedColor[1],fixedColor[2]);

        // Create the image
        WritableImage image = createImageWithColor(300, 300, color);

        // Save the image to a file
        saveImageToFile(image, "C:\\Temp\\output.png");

        System.out.printf("Fixed Color:  RGB  (%d, %d, %d)%n", fixedColor[0], fixedColor[1], fixedColor[2]);
        System.out.println("Fixed Color: HEX (" + rgbToHex(fixedColor[0], fixedColor[1], fixedColor[2])+")");


    }
    public Color rgbToColor(int red, int green, int blue) {
        // Ensure RGB values are within the range 0-255
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        // Create and return the Color object
        return Color.rgb(red, green, blue);
    }
    public WritableImage createImageWithColor(int width, int height, Color color) {
        // Create a WritableImage
        WritableImage image = new WritableImage(width, height);

        // Fill the image with the specified color
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.getPixelWriter().setColor(x, y, color);
            }
        }

        return image;
    }

    public void saveImageToFile(WritableImage image, String filePath) {
        try {
            // Save the image as a PNG file
            ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(image, null), "png", new File(filePath));
            System.out.println("Image saved as " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String rgbToHex(int red, int green, int blue) {
        // Ensure RGB values are within the range 0-255
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        // Convert to HEX
        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public static int[] fixContrast(int[] foreground, int[] background, double minContrastRatio) {
        double backgroundLuminance = calculateLuminance(background);
        double contrastRatio = calculateContrastRatio(foreground, backgroundLuminance);

        if (contrastRatio < minContrastRatio) {
            // Increase brightness of the foreground color
            return adjustColorBrightness(foreground, minContrastRatio, backgroundLuminance);
        }

        return foreground; // Return original color if contrast is sufficient
    }

    private static double calculateContrastRatio(int[] foreground, double backgroundLuminance) {
        double foregroundLuminance = calculateLuminance(foreground);
        return (foregroundLuminance + 0.05) / (backgroundLuminance + 0.05);
    }

    private static double calculateLuminance(int[] rgb) {
        double r = gammaCorrect(rgb[0] / 255.0);
        double g = gammaCorrect(rgb[1] / 255.0);
        double b = gammaCorrect(rgb[2] / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double gammaCorrect(double color) {
        if (color <= 0.03928) {
            return color / 12.92;
        } else {
            return Math.pow((color + 0.055) / 1.055, 2.4);
        }
    }

    private static int[] adjustColorBrightness(int[] color, double minContrastRatio, double backgroundLuminance) {
        // Gradually increase brightness until contrast ratio is met
        for (int i = 0; i < 255; i++) {
            int[] brightenedColor = {
                    Math.min(color[0] + i, 255),
                    Math.min(color[1] + i, 255),
                    Math.min(color[2] + i, 255)
            };
            double newContrastRatio = calculateContrastRatio(brightenedColor, backgroundLuminance);
            if (newContrastRatio >= minContrastRatio) {
                return brightenedColor;
            }
        }
        return color; // Fallback in case no adjustment achieves the goal
    }

}
