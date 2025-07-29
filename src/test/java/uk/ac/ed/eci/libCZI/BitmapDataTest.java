package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import uk.ac.ed.eci.libCZI.bitmaps.Bitmap;
import uk.ac.ed.eci.libCZI.bitmaps.BitmapData;

// Let's assume you created a new test class like this.
public class BitmapDataTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testBitmapCopyTo() throws Exception {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should exist.");
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(-123000, 30000, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);

        // 1. The outer try-with-resources manages the parent objects.
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
             CziStreamReader reader = CziStreamReader.fromStream(stream);
             SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader)) {

            // The accessor is now guaranteed to be open for this entire block.

            // 2. The inner try-with-resources manages the child objects.
            // These will be closed *before* the accessor.
            try (Bitmap bitmap = accessor.getBitmapRaw(roi, 0.25f);
                 BitmapData data = bitmap.getBitmapData()) {

                // 3. Perform your assertions here, safely inside the scope.
                byte[] imageData = data.getBytes();
                assertNotEquals(0, imageData.length, "Image data should not be empty.");

                byte result = (byte) 0xFF;
                for (byte b : imageData) {
                    result &= b;
                }
                assertNotEquals((byte) 0xFF, result, "Image should not be all white.");
            }
            // <-- At this point, `data.close()` and `bitmap.close()` have been called.

        } // <-- Now, `accessor.close()` (with the free() call restored) is safely called.
    }

    @Test
    public void testBitmapZoom() throws Exception {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should exist.");
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(-123000, 30000, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);
        
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
            CziStreamReader reader = CziStreamReader.fromStream(stream);
            SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader)) {

            // First zoom level
            try (Bitmap bitmap = accessor.getBitmapRaw(roi, 0.25f);
                 BitmapData data = bitmap.getBitmapData())  {
                byte[] imageData = data.getBytes();
                assertNotEquals(0, imageData.length, "Image data at 0.25f zoom should not be empty.");
            }

            // Second zoom level
            try (Bitmap bitmap = accessor.getBitmapRaw(roi, 0.5f);
                 BitmapData data = bitmap.getBitmapData()) {
                byte[] imageData = data.getBytes();
                assertNotEquals(0, imageData.length, "Image data at 0.5f zoom should not be empty.");
            }
        }
    }
}