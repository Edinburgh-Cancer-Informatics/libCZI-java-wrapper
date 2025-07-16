package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class CZIInputStreamTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testOpenFileAndGetReader() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        assertEquals(0, streamResult.errorCode(), "Should open stream without error.");
        assertNotNull(streamResult.stream(), "Stream handle should not be null.");

    }
}
