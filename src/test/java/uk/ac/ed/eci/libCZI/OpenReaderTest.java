package uk.ac.ed.eci.libCZI;
    
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class OpenReaderTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testOpenReaderFromFileStream() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        OpenReader reader = OpenReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        assertEquals(0, reader.errorCode(), "Should open reader without error.");
        assertNotNull(reader.reader(), "Reader handle should not be null.");
    }

    @Test
    public void testSubBlockCount() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        OpenReader reader = OpenReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        SubBlockStatistics stats = reader.simpleReaderStatistics();
        assertEquals(150, stats.subBlockCount());  
    }
}
