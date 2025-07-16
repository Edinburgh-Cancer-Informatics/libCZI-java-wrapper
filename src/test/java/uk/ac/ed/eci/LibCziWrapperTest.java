package uk.ac.ed.eci;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class LibCziWrapperTest {

    private static final Path TEST_IMAGE_PATH = Paths.get("test-images", "test-image.czi");

    @Test
    public void testGetLibCZIVersionInfo() {
        LibCziFFM.VersionInfo version = LibCziFFM.getLibCZIVersionInfo();
        assertNotNull(version);
        assertTrue(version.major() >= 0); // Example assertion: major version should be non-negative
        assertTrue(version.minor() >= 66); // Example assertion: minor version should be non-negative
        // Add more assertions to validate the version information
    }

    @Test
    public void testOpenFileAndGetReader() {
        assertTrue(Files.exists(TEST_IMAGE_PATH), "Test image should have been downloaded by Maven.");

        LibCziFFM.InputStreamResult streamResult = LibCziFFM.createInputStreamFromFileUTF8(TEST_IMAGE_PATH.toString());
        assertEquals(0, streamResult.errorCode(), "Should open stream without error.");
        assertNotNull(streamResult.stream(), "Stream handle should not be null.");

        // Add further tests using the opened stream...
    }
}
