package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import uk.ac.ed.eci.libCZI.bitmaps.Bitmap;
import uk.ac.ed.eci.libCZI.bitmaps.BitmapData;
import uk.ac.ed.eci.libCZI.bitmaps.BitmapInfo;
import uk.ac.ed.eci.libCZI.bitmaps.BitmapLockInfo;

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

    @Test
    public void testLockUnlock() throws Exception {
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(40960, 4096, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
                CziStreamReader reader = CziStreamReader.fromStream(stream);
                SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader);
                Bitmap bitmap = accessor.getBitmap(roi, 1.0f)) {

            BitmapLockInfo lock = bitmap.lock();
            assertNotNull(lock);
            assertEquals(WIDTH_HIGHT_IN_BYTES * 3, lock.stride());
            assertEquals(WIDTH_HIGHT_IN_BYTES * WIDTH_HIGHT_IN_BYTES * 3, lock.size());

            bitmap.unlock();
        }
    }

    @Test
    public void testBitmapCopyTo() throws Exception {
        final int WIDTH_HIGHT_IN_BYTES = 1024;
        IntRect roi = new IntRect(-123000, 30000, WIDTH_HIGHT_IN_BYTES, WIDTH_HIGHT_IN_BYTES);
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
                CziStreamReader reader = CziStreamReader.fromStream(stream);
                SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader);
                Bitmap bitmap = accessor.getBitmap(roi, 0.25f);
                BitmapData data = bitmap.getBitmapData())
         {
            byte[] imageData = data.getBytes();
            assertNotEquals(0, imageData.length, "Image data should not be empty.");

            // Check if all the pixels are white, this has been an error in the passed
            //
            byte result = (byte) 0xFF;
            for(int x = 0; x < imageData.length; x++) {
                result &= imageData[x];
            }

            assertNotEquals(0xFF, result, "Image should not be all white.");
        }
    }
}
