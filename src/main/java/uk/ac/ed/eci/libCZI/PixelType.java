package uk.ac.ed.eci.libCZI;

/**
 * Represents the pixel types supported by the libCZI library.
 * This enum corresponds to the `PixelType` enum in the libCZI C API.
 * It defines various pixel formats, including grayscale, BGR, and complex float types,
 * along with their associated integer values.
 *
 * @see <a href="https://zeiss.github.io/libczi/lib/enum_namespacelib_c_z_i_1abf8ce12ab88b06c8b3b47efbb5e2e834.html">PixelType</a>
 * @author Paul Mitchell
 */
public enum PixelType {
    Invalid(0xFF),
    Gray8(0),
    Gray16(1),
    Gray32Float(2),
    Bgr24(3),
    Bgr48(4),
    Bgr96Float(8),
    Bgra32(9), // Currently not supported in libCZI.
    Gray64ComplexFloat(10), // Currently not supported in libCZI.
    Bgr192ComplexFloat(11), // Currently not supported in libCZI.
    Gray32(12), // Currently not supported in libCZI.
    Gray64Float(13); // Currently not supported in libCZI.

    private final int value;

    PixelType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PixelType fromValue(int value) {
        for (PixelType type : PixelType.values()) {
            if (type.value == value) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown PixelType value: " + value);
    }

}
