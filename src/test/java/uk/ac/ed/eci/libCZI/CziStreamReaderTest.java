package uk.ac.ed.eci.libCZI;
    
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class CziStreamReaderTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testOpenReaderFromFileStream() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        CziStreamReader reader = CziStreamReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        assertEquals(0, reader.errorCode(), "Should open reader without error.");
        assertNotNull(reader.reader(), "Reader handle should not be null.");
    }

    @Test
    public void testSubBlockCount() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        CziStreamReader reader = CziStreamReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        SubBlockStatistics stats = reader.simpleReaderStatistics();
        assertEquals(150, stats.subBlockCount());  
    }

    @Test
    public void testBoundingBoxes() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        CziStreamReader reader = CziStreamReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        SubBlockStatistics stats = reader.simpleReaderStatistics();
        IntRect bb = stats.boundingBox();
        IntRect bb0 = stats.boundingBoxLayer0();

        assertEquals(-123977, bb.x());
        assertEquals(28824, bb.y());
        assertEquals(50171, bb.w());
        assertEquals(11349, bb.h());

        // Layer 0 BB
        assertEquals(-123977, bb0.x());
        assertEquals(28824, bb0.y());
        assertEquals(50159, bb0.w());
        assertEquals(11338, bb0.h());
    }

    @Test
    public void testMIndex() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        CziStreamReader reader = CziStreamReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        SubBlockStatistics stats = reader.simpleReaderStatistics();
        
        assertEquals(0, stats.minMIndex());
        assertEquals(26, stats.maxMIndex());
    }

    @Test
    public void testAttachmentCount() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        CZIInputStream streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        CziStreamReader reader = CziStreamReader.fromStream(streamResult);
        assertNotNull(reader, "Reader should not be null.");
        
        int attachmentCount = reader.attachmentCount();
        assertEquals(6, attachmentCount);
    }
}
