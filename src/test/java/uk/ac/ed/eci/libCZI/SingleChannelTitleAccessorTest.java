package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SingleChannelTitleAccessorTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testCreateAndFreeAccessor() throws Exception {
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
             CziStreamReader reader = CziStreamReader.fromStream(stream);
             SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader)) {
            // The accessor is created and freed in the try-with-resources block.
            // If no exception is thrown, the test passes.
        }
    }

    @ParameterizedTest(name = "zoom={0}, expected width={1}, expected height={2}")
    @CsvSource({
            "1.0, 1024, 1024",
            "0.5, 512, 512",
            "0.25, 256, 256",
            "0.1, 102, 102"
    })
    public void testCalcTileSize(float zoom, int expectedWidth, int expectedHeight) throws Exception {
        IntRect roi = new IntRect(40960,4096,1024,1024);
        try (CZIInputStream stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
             CziStreamReader reader = CziStreamReader.fromStream(stream);
             SingleChannelTileAccessor accessor = new SingleChannelTileAccessor(reader)) {
             IntSize size = accessor.calcTileSize(roi, zoom);

             assertEquals(expectedWidth, size.w());
             assertEquals(expectedHeight, size.h());
        }
    }
}
