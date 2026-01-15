package uk.ac.ed.eci.libCZI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.foreign.MemorySegment;

import org.junit.jupiter.api.Test;

import uk.ac.ed.eci.libCZI.LibCziFFM;
import uk.ac.ed.eci.libCZI.metadata.BuildInformation;
import uk.ac.ed.eci.libCZI.metadata.LibraryVersion;

public class LibCziFFMTest {
    @Test
    public void testLoadLibrary() {
        // This test simply attempts to load the library.
        // If it throws an exception, the test will fail.
        // The actual loading is done statically when LibCziFFM is accessed.
        // We just need to ensure no exceptions are thrown during static initialization.
        LibCziFFM.SYMBOL_LOOKUP.toString(); // Access a static field to trigger static init
    }

    @Test
    public void testLibraryBasedMemoryManagement() {
        final long size = 4096;
        MemorySegment segment = LibCziFFM.GLOBAL_ARENA.allocate(size);
        assertNotEquals(0L, segment.address());
        LibCziFFM.free(segment);
    }

    @Test
    public void testGetLibraryVersion() {
        LibraryVersion version = LibCziFFM.getLibraryVersion();
        assertNotEquals(null, version);
        assertEquals(0, version.getMajor(), "Major version should be 0");
        assertEquals(67, version.getMinor(), "Minor version should be 67");
        assertEquals(3, version.getPatch(), "Patch version should be 3");
        assertEquals(0, version.getTweak(), "Tweak version should be 0");
    }

    @Test
    public void testGetLibraryBuildInformation() {
        BuildInformation buildInfo = LibCziFFM.getLibraryBuildInformation();
        assertNotEquals(null, buildInfo);
        assertEquals("GNU 12.2.0", buildInfo.getCompilerIdentification());
        assertEquals("https://github.com/ZEISS/libczi.git", buildInfo.getRepositoryUrl());
        assertEquals("main~1", buildInfo.getRepositoryBranch());
        assertEquals("fe30d63426895b1acef5c8d1ca60235867895e11".length(), buildInfo.getRepositoryTag().length());
    }
}
