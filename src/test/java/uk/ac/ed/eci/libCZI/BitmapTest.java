package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class BitmapTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testGetBitmapInfo() throws Exception {
        IntRect roi = new IntRect(40960, 4096, 1024, 1024);
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
             CziStreamReader reader = CziStreamReader.fromStream(stream);
             SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader);
             Bitmap bitmap = accessor.getBitmap(roi, 1.0f)) {

            BitmapInfo info = bitmap.getBitmapInfo();
            assertNotNull(info);
            assertEquals(1024, info.width());
            assertEquals(1024, info.height());
            assertEquals(PixelType.Bgr24, info.pixelType());
        }
    }
}
