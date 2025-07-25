package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ed.eci.libCZI.bitmaps.Bitmap;
import uk.ac.ed.eci.libCZI.bitmaps.BitmapData;

public class BitmapDataTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");
    // Declare fields for shared objects
    private CZIInputStream stream;
    private CziStreamReader reader;
    private SingleChannelTileAccessor accessor;

    @BeforeEach
    public void setup() throws Exception {
        // Ensure the test image exists before each test
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        // Initialize resources before each test
        stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        reader = CziStreamReader.fromStream(stream);
        accessor = new SingleChannelTileAccessor(reader);
    }

    @AfterEach
    public void teardown() throws Exception {
        // Close resources in reverse order of creation after each test
        if (accessor != null) {
            accessor.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (stream != null) {
            stream.close();
        }
    }    

    @Test
    public void testBitmapZoom() throws Exception {
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(-123000, 30000, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);
        
        try (Bitmap bitmap = accessor.getBitmap(roi, 0.25f);
             BitmapData data = bitmap.getBitmapData()) {
            byte[] imageData = data.getBytes();
            assertNotEquals(0, imageData.length, "Image data should not be empty.");
        }
        
        try (Bitmap bitmap = accessor.getBitmap(roi, 0.5f);
             BitmapData data = bitmap.getBitmapData()) {
            byte[] imageData = data.getBytes();
            assertNotEquals(0, imageData.length, "Image data should not be empty.");
        }
    }

    @Test
    public void testBitmapCopyTo() throws Exception {
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(-123000, 30000, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);
        try (Bitmap bitmap = accessor.getBitmap(roi, 0.25f);
             BitmapData data = bitmap.getBitmapData()) {
            byte[] imageData = data.getBytes();
            assertNotEquals(0, imageData.length, "Image data should not be empty.");

            byte result = (byte) 0xFF;
            for (byte b : imageData) {
                result &= b;
            }
            assertNotEquals((byte) 0xFF, result, "Image should not be all white.");
        }
    }
}
