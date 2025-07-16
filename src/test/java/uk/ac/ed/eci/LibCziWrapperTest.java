package uk.ac.ed.eci;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LibCziWrapperTest {

    @Test
    public void testGetLibCZIVersionInfo() {
        LibCziFFM.VersionInfo version = LibCziFFM.getLibCZIVersionInfo();
        assertNotNull(version);
        assertTrue(version.major() >= 0); // Example assertion: major version should be non-negative
        assertTrue(version.minor() >= 66); // Example assertion: minor version should be non-negative
        // Add more assertions to validate the version information
    }

    // Add more test methods for other functionalities
}
