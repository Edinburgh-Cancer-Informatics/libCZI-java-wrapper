package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class SingleChannelTileAccessorTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");
    private CZIInputStream stream;
    private CziStreamReader reader;
    private SingleChannelTileAccessor accessor;

    @BeforeEach
    public void setup() throws Exception {
        stream = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        reader = CziStreamReader.fromStream(stream);
        accessor = new SingleChannelTileAccessor(reader);
    }

    @AfterEach
    public void teardown() throws Exception {
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
    public void testCreateAndFreeAccessor() throws Exception {
        assertNotNull(accessor);
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
        IntSize size = accessor.calcTileSize(roi, zoom);

        assertEquals(expectedWidth, size.w());
        assertEquals(expectedHeight, size.h());
    }
}
