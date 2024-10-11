
package com.girbola.utils;

import static com.girbola.utils.ColorCalculations.fixContrast;
import static com.girbola.utils.ColorCalculations.rgbToHex;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class ColorCalculationsTest {


    @Test
    public void testFixContrastWithDecreasedContrastNeeded() {
        int[] backgroundColor = {255, 255, 255}; // White
        int[] foregroundColor = {100, 100, 100}; // Dark Gray

        int[] fixedColor = fixContrast(foregroundColor, backgroundColor, 4.5);
        System.out.printf("Fixed Color: RGB(%d, %d, %d)%n", fixedColor[0], fixedColor[1], fixedColor[2]);
    }

    @Test
    public void testSomeinthtrueTest() {
        ColorCalculations colorCalculations = new ColorCalculations("272727","5bffb2");

    }


    @Test
    void testFixContrastWithIncreasedContrastNeeded() {
        // Initialize test color data
        int[] foreground = {50, 50, 50};
        int[] background = {100, 100, 100};
        double minContrastRatio = 1.5;

        int[] contrastFixedColors = fixContrast(foreground, background, minContrastRatio);

        // Assert color was adjusted
        assertNotNull(contrastFixedColors);
        assertArrayEquals(new int[] {150, 150, 150}, contrastFixedColors);
    }

    @Test
    void testFixContrastWithNoChangeNeeded() {
        // Initialize test color data
        int[] foreground = {150, 150, 150};
        int[] background = {50, 50, 50};
        double minContrastRatio = 1.5;

        int[] contrastFixedColors = fixContrast(foreground, background, minContrastRatio);

        // Assert color remains the same
        assertNotNull(contrastFixedColors);
        assertArrayEquals(new int[] {150, 150, 150}, contrastFixedColors);
    }
}
