package common.utils;

import com.girbola.messages.Messages;

public class ImageOffsetUtils {

    private static final int JPEG_OFFSET_SHIFT = 12;

    private static final byte[] JPEG_MAGIC = new byte[] {
            (byte) 0xFF, (byte) 0xD8
    };

    private static final byte[] PNG_MAGIC = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private static final byte[] GIF_MAGIC = new byte[] {
            0x47, 0x49, 0x46, 0x38
    };

    private static boolean matchesMagic(byte[] data, int offset, byte[] magic) {
        if (data == null || offset < 0 || offset + magic.length > data.length) {
            return false;
        }

        for (int i = 0; i < magic.length; i++) {
            if ((data[offset + i] & 0xFF) != (magic[i] & 0xFF)) {
                return false;
            }
        }
        return true;
    }

    public static int resolveImageOffset(byte[] data, int offset) {
        if (data == null || offset < 0) {
            return offset;
        }
        Messages.sprintf("Resolving EXIF image offset. Offset: " + offset);
        if (matchesMagic(data, offset, JPEG_MAGIC) || matchesMagic(data, offset, PNG_MAGIC) || matchesMagic(data, offset, GIF_MAGIC)) {
            Messages.sprintf("Detected image, adjusting offset by 8 bytes. Offset: " + offset);
            return offset;
        }

        for (int testOffset = 0; testOffset < Math.min(data.length - JPEG_MAGIC.length, 256); testOffset++) {
            if (matchesMagic(data, offset + testOffset, JPEG_MAGIC)) {
                Messages.sprintf("Detected embedded JPEG at offset +" + testOffset + " bytes");
                return offset + testOffset;
            }
        }

        return offset;
    }


}
