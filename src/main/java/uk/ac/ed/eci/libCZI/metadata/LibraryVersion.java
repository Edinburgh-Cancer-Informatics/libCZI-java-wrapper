package uk.ac.ed.eci.libCZI.metadata;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

import uk.ac.ed.eci.libCZI.IInterop;

/**
 * <b>LibCZIVersionInfoInterop</b>
 * This struct contains the version information of the libCZIApi-library. 
 * For versioning libCZI, SemVer2 (https://semver.org/) is used. 
 * Note that the value of the tweak version number does not 
 * have a meaning (as far as SemVer2 is concerned).
 * @see https://zeiss.github.io/libczi/api/struct_lib_c_z_i_version_info_interop.html
 * 
 */
public class LibraryVersion implements IInterop {
    private int major;
    private int minor;
    private int patch;
    private int tweak;

    public LibraryVersion(int major, int minor, int patch, int tweak) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.tweak = tweak;
    }
    public int getMajor() {
        return major;
    }
    public int getMinor() {
        return minor;
    }
    public int getPatch() {
        return patch;
    }
    public int getTweak() {
        return tweak;
    }
    @Override
    public String toString() {
        return major + "." + minor + "." + patch + "." + tweak;
    }

    @Override
    public MemorySegment toMemorySegment(Arena arena) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toMemorySegment'");
    }

    public static LibraryVersion fromMemorySegment(MemorySegment segment) {
        return new LibraryVersion(
            segment.get(JAVA_INT, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("major"))),
            segment.get(JAVA_INT, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("minor"))),
            segment.get(JAVA_INT, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("patch"))),
            segment.get(JAVA_INT, LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("tweak")))
        );
    }

    public static final MemoryLayout LAYOUT = MemoryLayout.structLayout(
        JAVA_INT.withName("major"),
        JAVA_INT.withName("minor"),
        JAVA_INT.withName("patch"),
        JAVA_INT.withName("tweak")
    );
}
