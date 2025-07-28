package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.ed.eci.libCZI.metadata.Metadata;

public class MetadataTest {
    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");
    private CziStreamReader reader;
    private CZIInputStream streamResult;

    @BeforeEach
    public void setup() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        streamResult = CZIInputStream.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        reader = CziStreamReader.fromStream(streamResult);
    }

    @AfterEach
    public void teardown() throws Exception {
        reader.close();
        streamResult.close();
    }

    @Test
    public void testMetadata() throws Exception {
        Metadata metadata = reader.metadata();
        assertNotNull(metadata, "Metadata should not be null.");        
    }
}
